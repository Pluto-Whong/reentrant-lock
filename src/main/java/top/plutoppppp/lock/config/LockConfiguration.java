package top.plutoppppp.lock.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import top.plutoppppp.lock.AbsReentrantLockServer;
import top.plutoppppp.lock.LockActuator;
import top.plutoppppp.lock.aop.LockAspect;
import top.plutoppppp.lock.server.impl.ReentrantRedisLockServerImpl;

/**
 * 
 * <p>
 * 分布式锁，spring自动配置
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:46:31
 */
@Configuration
@AutoConfigureAfter(LockRedisConfig.class)
public class LockConfiguration {

	public static final String lockConfigPrefix = "lock";

	@Bean
	@ConfigurationProperties(prefix = lockConfigPrefix + ".default")
	public LockConstants lockConstants() {
		return LockConstants.INSTANCE;
	}

	/**
	 * 可重入分布式锁服务
	 *
	 * @param redisLockTemplate 这样写的话会优先根据bean名称注入，若没有则会注入primary
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 23:27:24
	 */
	@Bean
	public AbsReentrantLockServer reentrantLockServer(RedisTemplate<String, ?> redisLockTemplate) {
		ReentrantRedisLockServerImpl impl = new ReentrantRedisLockServerImpl();
		impl.setRedisTemplate(redisLockTemplate);
		return impl;
	}

	@Bean
	public LockActuator lockActuator(AbsReentrantLockServer lockServer) {
		LockActuator.INSTANCE.setLockServer(lockServer);
		return LockActuator.INSTANCE;
	}

	@Bean
	public LockAspect lockAspect() {
		return new LockAspect();
	}

}