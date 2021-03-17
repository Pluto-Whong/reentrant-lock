package top.plutoppppp.lock.server.impl;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import top.plutoppppp.lock.AbsReentrantLockServer;
import top.plutoppppp.lock.ReentrantLockInfo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <p>
 * 通过 redis 锁服务，可重入的
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:51:36
 */
@Slf4j
public class ReentrantRedisLockServerImpl extends AbsReentrantLockServer {

	private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>(
			"return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])", String.class);
	private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>(
			"if redis.call('get',KEYS[1]) == ARGV[1] then return tostring(redis.call('del', KEYS[1])==1) else return 'false' end",
			String.class);
	private static final String LOCK_SUCCESS = "OK";

	@Setter
	private RedisTemplate<String, ?> redisTemplate;

	/**
	 * 从redis获取锁
	 * 
	 * @param lockInfo
	 * @return
	 */
	private boolean acquireRedis(ReentrantLockInfo lockInfo) {
		String lockResult = redisTemplate.execute(SCRIPT_LOCK, redisTemplate.getStringSerializer(),
				redisTemplate.getStringSerializer(), Collections.singletonList(lockInfo.getLockKey()),
				lockInfo.getLockValue(), String.valueOf(lockInfo.getExpire()));
		return LOCK_SUCCESS.equals(lockResult);
	}

	@Override
	protected boolean concurrentOnece(ReentrantLockInfo lockInfo) throws Exception {
		return this.acquireRedis(lockInfo);
	}

	@Override
	protected boolean concurrentPreemptive(ReentrantLockInfo lockInfo) throws Exception {
		String lockKey = lockInfo.getLockKey();
		long overtimeMillis = lockInfo.getOvertime();
		long waittimeMillis = lockInfo.getWaittime();

		long overtime = System.currentTimeMillis() + overtimeMillis;

		try {
			for (int queryCount = 0; System.currentTimeMillis() <= overtime; TimeUnit.MILLISECONDS
					.sleep(waittimeMillis), queryCount++) {
				boolean acquireRedis = this.acquireRedis(lockInfo);
				if (acquireRedis) {
					log.trace("lockKey[{}] count[{}]", lockKey, queryCount);
					return true;
				}
			}
		} catch (InterruptedException e) {
			return false;
		}
		return false;
	}

	@Override
	protected boolean unlockRealize(ReentrantLockInfo lockInfo) throws Exception {
		Object releaseResult = redisTemplate.execute(SCRIPT_UNLOCK, redisTemplate.getStringSerializer(),
				redisTemplate.getStringSerializer(), Collections.singletonList(lockInfo.getLockKey()),
				lockInfo.getLockValue());
		return Boolean.valueOf(releaseResult.toString());
	}

}