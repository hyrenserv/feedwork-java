package fd.ng.core.exception.internal;

public class RevitalizedCheckedException extends RuntimeException {
	private static final long serialVersionUID = 2187712583686565757L;
	private final String mineMessage;   // 构造这个异常对象时，提供的信息
	private final String causeMessage;  // 被包装的原始异常的 getMessage() 函数得到的信息串
	static {
		NestedExceptionUtils.class.getName();
	}

	public RevitalizedCheckedException() {
		this.mineMessage = null;
		this.causeMessage = null;
	}
	/**
	 * 使用这个构造函数，意味着实际没有异常发生，仅仅是构造了一个 RuntimeException 而已。
	 * 目的是把自己希望抛到外面的提示信息构造出来
	 * @param msg String 自己提供的异常提示信息
	 */
	public RevitalizedCheckedException(String msg) {
		super(msg);
		this.mineMessage = msg;
		this.causeMessage = null;
	}
	/**
	 * 把真正发生的异常封装起来，且不提供自己的提示信息。
	 * 通过 getMessage() 实际得到的是原异常的 message。
	 * 只对受检异常用这个构造函数进行封装后抛出。
	 * @param cause Throwable 真正发生的异常。
	 */
	public RevitalizedCheckedException(Throwable cause) {
		super(cause);
		this.mineMessage = null;
		this.causeMessage = cause.getMessage();
	}

	/**
	 * 把真正发生的异常封装起来，并且提供了自己的提示信息。
	 * 通过 getMessage() 得到的是构造异常的信息和原始异常信息。如果要得到原异常的 message，应调用 getCauseMessage() 。
	 * @param canbeGettedMessage String 创建异常时，提供的提示信息。通过 getMineMessage() 可获取
	 * @param cause Throwable 真正发生的异常。
	 */
	public RevitalizedCheckedException(String canbeGettedMessage, Throwable cause) {
		super(canbeGettedMessage, cause);
		this.mineMessage = canbeGettedMessage;
		this.causeMessage = cause.getMessage();
	}


	@Override
	public String getMessage() {
		return NestedExceptionUtils.buildMessage(this.mineMessage, getCause());
	}

	public String getMineMessage() {
		return this.mineMessage;
	}
	public String getCauseMessage() {
		return this.causeMessage;
	}

	public Throwable getRootCause() {
		return NestedExceptionUtils.getRootCause(this);
	}

	public Throwable getMostSpecificCause() {
		Throwable rootCause = getRootCause();
		return (rootCause != null ? rootCause : this);
	}

	public boolean contains(Class<?> exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		Throwable cause = getCause();
		if (cause == this) {
			return false;
		}
		if (cause instanceof RevitalizedCheckedException) {
			return ((RevitalizedCheckedException) cause).contains(exType);
		}
		else {
			while (cause != null) {
				if (exType.isInstance(cause)) {
					return true;
				}
				if (cause.getCause() == cause) {
					break;
				}
				cause = cause.getCause();
			}
			return false;
		}
	}
}
