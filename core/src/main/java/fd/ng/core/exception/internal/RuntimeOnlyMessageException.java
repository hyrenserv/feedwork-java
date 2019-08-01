package fd.ng.core.exception.internal;

/**
 * 仅仅用于抛出提示消息的场景。
 * 本异常不记录异常发生的堆栈，所以：
 * 1）性能极其好
 * 2）无法跟踪定位异常发生的位置
 */
public class RuntimeOnlyMessageException extends BaseInternalRuntimeException {
	private static final long serialVersionUID = -982189525378065729L;

	public RuntimeOnlyMessageException() {
	}

	public RuntimeOnlyMessageException(String message) {
		super(message);
	}

	public RuntimeOnlyMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeOnlyMessageException(Throwable cause) {
		super(cause);
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
	@Override
	public Throwable initCause(Throwable cause) {
		return this;
	}
}
