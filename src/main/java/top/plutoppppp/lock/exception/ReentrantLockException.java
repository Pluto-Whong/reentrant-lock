package top.plutoppppp.lock.exception;

/**
 * 
 * <p>
 * 可重入锁异常
 * </p>
 *
 * @author wangmin07@hotmail.com
 * @since 2020-11-25 17:19:43
 */
public class ReentrantLockException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReentrantLockException() {
		super();
	}

	public ReentrantLockException(String message) {
		super(message);
	}

	public ReentrantLockException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReentrantLockException(Throwable cause) {
		super(cause);
	}

	protected ReentrantLockException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
