package fd.ng.core.exception.internal;

import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.UuidUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 作为 RawlayerRuntimeException 和 FrameworkRuntimeException 的父类。
 * 用于对系统内部错误的封装。本类一般不需要直接使用，使用子类即可。
 */
public class BaseInternalRuntimeException extends RevitalizedCheckedException {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -7306921151104598982L;
	public static final String ERRCODE_LOGPREFIX = " LogCode=";
	/**
	 * 通过 threadId_Millis 获得。因为毫秒内不会有重复的异常发生.
	 * 即使特殊性下真的发生了重复值，也很特例的，用户仍然可以找到相应的日志。
	 * 将来真的有大量重复值，可以改用 uuid
	 */
	protected final String errorCode;
	/**
	 * 被包装的异常，可能会有很多详细的信息仅仅需要打印到日志中，不需要前端了解。
	 * 即：通过 getMessage() 不会得到这个信息。
	 */
	protected final String loggedMessage;

	public BaseInternalRuntimeException() {
		this.errorCode = StringUtil.EMPTY;
		this.loggedMessage = null;
	}
	public BaseInternalRuntimeException(String msg) {
		super(msg);
		this.loggedMessage = null;
		errorCode = UuidUtil.threadId_Millis();
	}

	public BaseInternalRuntimeException(Throwable cause) {
		super(cause);
		this.loggedMessage = null;
		errorCode = UuidUtil.threadId_Millis();
	}

	public BaseInternalRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		this.loggedMessage = null;
		errorCode = UuidUtil.threadId_Millis();
	}

	public BaseInternalRuntimeException(String canbeGettedMessage, String loggedMessage) {
		super(canbeGettedMessage);
		this.loggedMessage = loggedMessage;
		errorCode = UuidUtil.threadId_Millis();
	}

	public BaseInternalRuntimeException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, cause);
		this.loggedMessage = loggedMessage;
		errorCode = UuidUtil.threadId_Millis();
	}

	public String getErrorCode() { return this.errorCode; }
	public String getLoggedMessage() { return this.loggedMessage; }

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getLocalizedMessage();
		String rs = (message != null) ? (s + ": " + message) : s;
		return (loggedMessage==null) ? rs : rs + " ... " + loggedMessage;
	}
}
