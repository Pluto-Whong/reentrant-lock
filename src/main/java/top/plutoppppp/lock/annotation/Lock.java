package top.plutoppppp.lock.annotation;

import java.lang.annotation.*;

import top.plutoppppp.lock.enumeration.ConcurrentType;

/**
 * 
 * <p>
 * 分布式锁
 * </p>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

	/**
	 * 锁名称
	 */
	String value() default "";

	/**
	 * 锁定key，如果为存在值，则value不生效，一般与其他项目使用同一锁的情况
	 */
	String lockKey() default "";

	/**
	 * 并发方式
	 */
	ConcurrentType concurrentType() default ConcurrentType.DEFAULT;

	/**
	 * 锁最高持续时间（即redis中的超时时间，ms）
	 * 
	 * 负数为使用默认时间
	 */
	long expire() default -1L;

	/**
	 * 获取锁超时时间
	 * 
	 * 负数为使用默认时间
	 */
	long overtimeMillis() default -1L;

	/**
	 * 抢占等待时间，只有concurrentType为PREEMPTIVE才有效
	 * 
	 * 负数为使用默认时间
	 */
	long waittimeMillis() default -1L;

}
