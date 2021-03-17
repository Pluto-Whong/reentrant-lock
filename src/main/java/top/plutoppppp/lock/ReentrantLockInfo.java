package top.plutoppppp.lock;

import java.util.UUID;

import top.plutoppppp.lock.annotation.Lock;
import top.plutoppppp.lock.config.LockConstants;
import top.plutoppppp.lock.enumeration.ConcurrentType;
import top.plutoppppp.lock.exception.ReentrantLockException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * <p>
 * 锁信息
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:49:57
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReentrantLockInfo {

	/**
	 * 锁定key，如果存在则直接使用该值作为key，如果为null则会根据前缀和lockName进行组装组合
	 */
	private String lockKey;

	/**
	 * 锁定时赋值，在解锁时为防止解锁了其他的线程，则需要验证这个值，所以在重入锁时要保证lockValue相同
	 */
	private String lockValue;

	/**
	 * 并发方式
	 */
	private ConcurrentType concurrentType;

	/**
	 * 锁持有时间（即redis中锁失效时间，ms）
	 */
	private long expire;

	/**
	 * 获取锁超时时间（ms）
	 */
	private long overtime;

	/**
	 * 轮询抢占时间间隔（ms）
	 */
	private long waittime;

	/**
	 * 保持量，做重入锁时线程持有量判断
	 */
	private int holdCount = 0;

	/**
	 * 递增保持量
	 * 
	 * @return
	 */
	int incrHoldCount() {
		this.holdCount++;
		return this.holdCount;
	}

	/**
	 * 递减保持量
	 * 
	 * @return
	 */
	int decrHoldCount() {
		this.holdCount--;
		return this.holdCount;
	}

	/**
	 * 线程是否全部释放
	 * 
	 * @return
	 */
	public boolean emptyHold() {
		return this.holdCount <= 0;
	}

	/**
	 * 
	 * <p>
	 * 可重入锁构建器
	 * </p>
	 *
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 22:57:03
	 */
	@Data
	@Accessors(fluent = true, chain = true)
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public final static class ReentrantLockBuilder {

		/**
		 * 锁名，前面会自动组装默认前缀
		 *
		 * @param lockName
		 * @return
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 20:53:32
		 */
		public static ReentrantLockBuilder lockName(String lockName) {
			return lockKey(String.format("%s%s", LockConstants.getPrefixKey(), lockName));
		}

		/**
		 * 锁key，使用输入值，一般用于跨应用时锁定使用
		 *
		 * @param lockKey
		 * @return
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 20:54:20
		 */
		public static ReentrantLockBuilder lockKey(String lockKey) {
			ReentrantLockBuilder builder = new ReentrantLockBuilder();
			builder.setLockKey(lockKey);
			return builder;
		}

		/**
		 * 根据注解Lock进行构造
		 *
		 * @param lock
		 * @return
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 21:06:54
		 */
		public static ReentrantLockBuilder lockAnno(Lock lock) {
			ReentrantLockBuilder builder;

			if (lock.lockKey().isEmpty()) {
				builder = lockName(lock.value());
			} else {
				builder = lockKey(lock.lockKey());
			}

			if (!ConcurrentType.DEFAULT.equals(lock.concurrentType())) {
				builder.concurrentType(lock.concurrentType());
			}

			if (lock.expire() < 0) {
				builder.expire(LockConstants.getExpireMillis());
			} else {
				builder.expire(lock.expire());
			}

			if (lock.overtimeMillis() < 0) {
				builder.overtime(LockConstants.getOvertimeMillis());
			} else {
				builder.overtime(lock.overtimeMillis());
			}

			if (lock.waittimeMillis() < 0) {
				builder.waittime(LockConstants.getWaittimeMillis());
			} else {
				builder.waittime(lock.waittimeMillis());
			}

			return builder;
		}

		/**
		 * 尝试锁定，若失败则抛出异常
		 *
		 * @return
		 * @throws ReentrantLockException
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 21:55:50
		 */
		public CloseableLockHallows trylock() throws ReentrantLockException {
			ReentrantLockInfo lockInfo = new ReentrantLockInfo(lockKey, UUID.randomUUID().toString(), concurrentType,
					expire, overtime, waittime, 0);

			CloseableLockHallows closeableLockHallows = CloseableLockHallows.of(lockInfo);

			closeableLockHallows.lock();

			return closeableLockHallows;
		}

		/**
		 * 锁定key
		 */
		@Setter(AccessLevel.PRIVATE)
		@Accessors(chain = true)
		private String lockKey;

		/**
		 * 并发方式
		 */
		private ConcurrentType concurrentType = LockConstants.getConcurrentType();

		/**
		 * 锁持有时间（即redis中锁失效时间，ms）
		 */
		private long expire = LockConstants.getExpireMillis();

		/**
		 * 获取锁超时时间（ms）
		 */
		private long overtime = LockConstants.getOvertimeMillis();

		/**
		 * 轮询抢占时间间隔（ms）
		 */
		private long waittime = LockConstants.getWaittimeMillis();

	}

	/**
	 * 
	 * <p>
	 * 锁之圣器
	 * 
	 * 配合try-resource-catch使用进行自动解锁
	 * </p>
	 *
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 13:48:19
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class CloseableLockHallows implements AutoCloseable {

		private static CloseableLockHallows of(ReentrantLockInfo lockInfo) {
			CloseableLockHallows hallows = new CloseableLockHallows();
			hallows.lockInfo = lockInfo;
			return hallows;
		}

		private boolean isLock = false;
		private boolean isUnlock = false;

		private ReentrantLockInfo lockInfo;

		/**
		 * 锁定，且只能执行一次，锁定失败会抛出异常
		 *
		 * @throws ReentrantLockException
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 22:05:52
		 */
		void lock() throws ReentrantLockException {
			if (isLock) {
				throw new ReentrantLockException(String.format("锁[%s]已经进行锁定，不得重复执行", lockInfo.getLockKey()));
			}

			boolean lock = LockActuator.lock(lockInfo);

			if (lock) {
				isLock = true;
			} else {
				throw new ReentrantLockException(String.format("获取锁[%s]失败", lockInfo.getLockKey()));
			}
		}

		/**
		 * 解锁，只能执行一次且在之前要进行一次成功加锁
		 *
		 * @return
		 * @throws ReentrantLockException
		 * @author wangmin07@hotmail.com
		 * @since 2020-11-25 22:06:16
		 */
		public boolean unlock() throws ReentrantLockException {
			if (!isLock) {
				throw new ReentrantLockException(String.format("只能对已锁定的锁[%s]进行解锁", lockInfo.getLockKey()));
			}
			if (isUnlock) {
				throw new ReentrantLockException(String.format("锁[%s]已经进行过解锁，不得重复执行", lockInfo.getLockKey()));
			}

			isUnlock = true;

			return LockActuator.releaseLock(lockInfo);
		}

		@Override
		public void close() throws ReentrantLockException {
			this.unlock();
		}

	}

}