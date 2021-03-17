package top.plutoppppp.lock.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import top.plutoppppp.lock.ReentrantLockInfo.CloseableLockHallows;
import top.plutoppppp.lock.ReentrantLockInfo.ReentrantLockBuilder;
import top.plutoppppp.lock.annotation.Lock;

/**
 * 
 * <p>
 * 分布式锁切面
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 13:47:17
 */
@Aspect
public class LockAspect {

	@Pointcut("@annotation(top.plutoppppp.lock.annotation.Lock)")
	private void lockPointcut() {

	}

	/**
	 * 环绕通知：灵活自由的在目标方法中切入代码
	 *
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 * @author wangmin07@hotmail.com
	 * @since 2020-11-25 13:47:26
	 */
	@Around("lockPointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		Lock lock = method.getDeclaredAnnotation(Lock.class);

		// 执行源方法
		try (CloseableLockHallows lockHallows = ReentrantLockBuilder.lockAnno(lock).trylock()) {
			return joinPoint.proceed();
		}
	}

}
