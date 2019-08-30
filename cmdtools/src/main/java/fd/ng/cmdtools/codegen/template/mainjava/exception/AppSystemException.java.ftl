package ${basePackage}.${subPackage};

import fd.ng.core.exception.BusinessSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
