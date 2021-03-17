package top.plutoppppp.lock.config;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * <p>
 * 分布式锁，redis自动配置
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:46:52
 */
@Configuration
@ConditionalOnProperty(prefix = LockRedisConfig.lockRedisPrefix, name = "hostName")
public class LockRedisConfig {

	public static final String lockRedisPrefix = LockConfiguration.lockConfigPrefix + ".redis";

	@Bean
	@Scope(value = "prototype")
	@ConfigurationProperties(prefix = lockRedisPrefix + ".pool")
	public GenericObjectPoolConfig<?> redisLockPool() {
		return new GenericObjectPoolConfig<>();
	}

	@Bean
	@ConfigurationProperties(prefix = lockRedisPrefix)
	public RedisStandaloneConfiguration redisConfigLock() {
		return new RedisStandaloneConfiguration();
	}

	@Bean
	public LettuceConnectionFactory factoryLock(@Qualifier("redisLockPool") GenericObjectPoolConfig<?> redisPool,
			@Qualifier("redisConfigLock") RedisStandaloneConfiguration redisConfig) {
		LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
				.poolConfig(redisPool).commandTimeout(Duration.ofMillis(redisPool.getMaxWaitMillis())).build();
		return new LettuceConnectionFactory(redisConfig, clientConfiguration);
	}

	@Bean
	public RedisTemplate<String, ?> redisLockTemplate(@Qualifier("factoryLock") LettuceConnectionFactory factory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(factory);
		template.afterPropertiesSet();
		return template;
	}

}
