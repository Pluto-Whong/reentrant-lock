package top.plutoppppp.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import top.plutoppppp.lock.exception.ReentrantLockException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * <p>
 * 锁执行器，主要面对“行”级锁
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 09:38:05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockActuator {

	public static final LockActuator INSTANCE = new LockActuator();

	private static ThreadLocal<Map<String, ReentrantLockInfo>> lockThreadLocal = new ThreadLocal<Map<String, ReentrantLockInfo>>();

	@Setter
	private AbsReentrantLockServer lockServer;

	/**
	 * 尝试锁起
	 *
	 * @param lockInfo
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 12:56:58
	 */
	private boolean trylock(ReentrantLockInfo lockInfo) {
		return lockServer.lock(lockInfo);
	}

	/**
	 * 解锁
	 *
	 * @param lockInfo
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 12:57:05
	 */
	private boolean unlock(ReentrantLockInfo lockInfo) {
		return lockServer.unlock(lockInfo);
	}

	/**
	 * <p>
	 * 根据lockInfo进行锁定
	 * <p>
	 * 注意，如果前面已经有了这个key的锁定并且还没有完全解除，那么再重入时，不会再根据新的lockInfo的配置进行抢占锁，即根据旧的也就是第一个
	 * <p>
	 * 锁定失败时会抛出ReentrantLockException异常
	 * </p>
	 * 
	 * @param lockInfo
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 12:57:38
	 */
	static boolean lock(ReentrantLockInfo lockInfo) {
		Map<String, ReentrantLockInfo> map = lockThreadLocal.get();
		if (map == null) {
			map = new HashMap<>();
			map.put(lockInfo.getLockKey(), lockInfo);
			lockThreadLocal.set(map);
		} else {
			// 线程重入，相当于单线程在操作这个map，所以不用担心并发问题
			if (map.containsKey(lockInfo.getLockKey())) {
				lockInfo = map.get(lockInfo.getLockKey());
			} else {
				map.put(lockInfo.getLockKey(), lockInfo);
			}
		}

		boolean trylock = LockActuator.INSTANCE.trylock(lockInfo);
		if (!trylock) {
			// 如果锁定失败，则检查是否无持有（即第一次锁定），如果是则清除掉threadLocal中的数据
			if (lockInfo.emptyHold()) {
				map.remove(lockInfo.getLockKey());
				if (map.isEmpty()) {
					lockThreadLocal.remove();
				}
			}
		}

		return trylock;
	}

	/**
	 * 根据lockKey解锁
	 *
	 * @param lockKey 锁定的key是输入的lockKey
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-24 18:02:58
	 */
	static boolean releaseLock(ReentrantLockInfo lockInfo) throws ReentrantLockException {
		Map<String, ReentrantLockInfo> map = lockThreadLocal.get();
		ReentrantLockInfo validLockInfo = map.get(lockInfo.getLockKey());
		if (Objects.isNull(validLockInfo)) {
			throw new ReentrantLockException("不存在的lockKey[" + lockInfo.getLockKey() + "]");
		}

		boolean unlock = LockActuator.INSTANCE.unlock(validLockInfo);
		if (validLockInfo.emptyHold()) {
			map.remove(validLockInfo.getLockKey());
			if (map.isEmpty()) {
				lockThreadLocal.remove();
			}
		}
		return unlock;
	}

}