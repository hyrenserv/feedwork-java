package fd.ng.core.exception;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;

/**
 * 项目中可直接使用的异常，用于对系统级异常的封装。
 * 业务处理代码中，如果发生了各种需要中断处理的情况，应该抛出 {@link BusinessProcessException}
 * 如果不属于以上范畴，则抛出本异常。
 * 大多数情况，都是 try...catch 的catch代码段中，对发生的异常进行再包裹后抛出。
 */
public class BusinessSystemException extends BaseInternalRuntimeException {
	private static final long serialVersionUID = 533231982882176445L;

	// 因为这个异常的使用场景是对系统内部异常的再抛出，所以，不应该构造无异常的对象。
	public BusinessSystemException(String msg) {
		super(msg);
	}

	public BusinessSystemException(Throwable cause) {
		super(cause);
	}

	public BusinessSystemException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 *
	 * @param canbeGettedMessage 反馈前端的消息，通过 getMessage() 获得
	 * @param loggedMessage 仅用于打印到日志中的消息
	 * @param cause 真正发生的异常
	 */
	public BusinessSystemException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, loggedMessage, cause);
	}
}
