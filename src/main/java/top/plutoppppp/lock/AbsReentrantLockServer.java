package top.plutoppppp.lock;

import top.plutoppppp.lock.enumeration.ConcurrentType;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <p>
 * 可重入分布式锁操作服务
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:51:15
 */
@Slf4j
public abstract class AbsReentrantLockServer {

	/**
	 * 获取锁
	 *
	 * @param lockInfo
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-24 14:05:03
	 */
	final boolean lock(ReentrantLockInfo lockInfo) {
		// 使用threadLocal做线程重入判断依据，如果持有者大于1个则直接返回为true
		// 因threadLocal根据线程获取值，并且重入以线程为依据，所以这里无需担心并发问题
		if (lockInfo.incrHoldCount() > 1) {
			return true;
		}

		try {
			boolean lockFlag;
			ConcurrentType concurrentType = lockInfo.getConcurrentType();
			switch (concurrentType) {
			case ONECE:
				lockFlag = concurrentOnece(lockInfo);
				break;
			case PREEMPTIVE:
				lockFlag = concurrentPreemptive(lockInfo);
				break;
			default:
				// QUERY之类的还没有实现
				throw new UnsupportedOperationException("不支持的并发类型");
			}

			if (!lockFlag) {
				lockInfo.decrHoldCount();
			}

			return lockFlag;
		} catch (Exception e) {
			log.error("锁定时出错", e);
			// 出错时进行一次减持
			lockInfo.decrHoldCount();
			return false;
		}
	};

	/**
	 * 默认并发方式，抢占一次，失败就报错退出
	 *
	 * @param lockInfo
	 * @return
	 * @throws Exception
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-24 14:05:13
	 */
	abstract protected boolean concurrentOnece(ReentrantLockInfo lockInfo) throws Exception;

	/**
	 * 循环抢占，直至超时
	 *
	 * @param lockInfo
	 */
	abstract protected boolean concurrentPreemptive(ReentrantLockInfo lockInfo) throws Exception;

	/**
	 * 解锁
	 *
	 * @param lockInfo
	 * @return
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-24 14:05:20
	 */
	final boolean unlock(ReentrantLockInfo lockInfo) {
		if (lockInfo.decrHoldCount() > 0) {
			return true;
		}

		try {
			return this.unlockRealize(lockInfo);
		} catch (Exception e) {
			log.error("解锁时出错", e);
			// 出错也算减持完成，但返回false通知异常
			return false;
		}
	}

	/**
	 * 解锁实现
	 *
	 * @param lockInfo
	 * @return
	 * @throws Exception
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-24 14:05:26
	 */
	abstract protected boolean unlockRealize(ReentrantLockInfo lockInfo) throws Exception;

}