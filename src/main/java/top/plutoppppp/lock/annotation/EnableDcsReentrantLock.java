package top.plutoppppp.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import top.plutoppppp.lock.config.LockConfiguration;
import top.plutoppppp.lock.config.LockRedisConfig;

/**
 * <p>
 * 开启分布式锁
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ LockRedisConfig.class, LockConfiguration.class })
public @interface EnableDcsReentrantLock {

}
