package top.plutoppppp.lock.enumeration;

public enum ConcurrentType {

	/**
	 * 默认方法，即从LockConstants获取到的默认值，这个值只是在Lock注解中占位使用，莫要当作普通枚举
	 */
	DEFAULT,
	/**
	 * 抢占一次，失败直接退出
	 */
	ONECE,
	/**
	 * 抢占式，循环抢占，直至失败
	 */
	PREEMPTIVE,
	/**
	 * 队列式，直接进入队列（未实现）
	 */
	// QUERY,

	/**
	 * 抢占队列，抢占一次，失败进入队列（未实现）
	 */
	// PREEMPTIVE_QUERY

}
