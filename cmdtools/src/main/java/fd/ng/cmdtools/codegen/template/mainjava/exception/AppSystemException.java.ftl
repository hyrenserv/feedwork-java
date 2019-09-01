package ${basePackage}.${subPackage};

import fd.ng.core.exception.BusinessSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 项目中可直接使用的异常，用于对系统级异常的封装。
 * 业务处理代码中，如果发生了各种需要中断处理的情况，应该抛出 {@link BusinessException}
 * 如果不属于以上范畴，则抛出本异常。
 * 大多数情况，都是 try...catch 的catch代码段中，对发生的异常进行再包裹后抛出。
 */
public class AppSystemException extends BusinessSystemException {
	private static final Logger logger = LogManager.getLogger();

	public AppSystemException(final String msg) {
		super(msg);
		logger.error(String.format("AppSystemException : %s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode));
	}

	public AppSystemException(Throwable cause) {
		super(cause);
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	public AppSystemException(String msg, Throwable cause) {
		super(msg, cause);
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	/**
	 *
	 * @param canbeGettedMessage 反馈前端的消息，通过 getMessage() 获得
	 * @param loggedMessage 仅用于打印到日志中的消息
	 * @param cause 真正发生的异常
	 */
	public AppSystemException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, loggedMessage, cause);
		logger.error(String.format("%s | %s%s | %s |", getMessage(), ERRCODE_LOGPREFIX, errorCode, loggedMessage), cause);
	}
}
