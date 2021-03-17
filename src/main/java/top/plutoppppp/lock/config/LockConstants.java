package top.plutoppppp.lock.config;

import top.plutoppppp.lock.enumeration.ConcurrentType;

import lombok.Setter;

/**
 * 
 * <p>
 * 锁默认属性设置
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:49:36
 */
@Setter
public final class LockConstants {

	public static final LockConstants INSTANCE = new LockConstants();

	/**
	 * 抢占类型，不要枚举default，这是特殊值，报错自己负责
	 */
	private ConcurrentType concurrentType = ConcurrentType.PREEMPTIVE;

	/**
	 * 锁定key前缀
	 */
	private String prefixKey = "common.lock.";

	/**
	 * 在redis中最大持有时间，即强制超时解锁时间，ms
	 */
	private long expireMillis = 60000L;

	/**
	 * 获取锁超时时间，ms
	 */
	private long overtimeMillis = 1000L;

	/**
	 * 抢占等待时间，即每次尝试抢锁需要等待的时间
	 */
	private long waittimeMillis = 1L;

	public static ConcurrentType getConcurrentType() {
		return LockConstants.INSTANCE.concurrentType;
	}

	public static String getPrefixKey() {
		return LockConstants.INSTANCE.prefixKey;
	}

	public static long getExpireMillis() {
		return LockConstants.INSTANCE.expireMillis;
	}

	public static long getOvertimeMillis() {
		return LockConstants.INSTANCE.overtimeMillis;
	}

	public static long getWaittimeMillis() {
		return LockConstants.INSTANCE.waittimeMillis;
	}

}
