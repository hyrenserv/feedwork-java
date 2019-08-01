package fd.ng.core.exception.internal;

import fd.ng.core.conf.AppinfoConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 系统框架产生的异常 FrameworkRuntimeException ，比如判断某个对象不能为空时抛出的异常，需要堆栈和异常信息
 * 抛出该异常时，如果使用自己描述的信息，需使用英文，因为该异常省去了对异常信息的i18n。
 * 该异常在 new 时自动打印日志，并生成 ERRCODE 用于日志定位。
 * WEB总控捕获后，给前端提示为固定英文（SFramework Internal Processing Error）和用于日志定位的 ERRCODE 。
 *
 * 重要的事情多说一次：
 * 该异常在 new 时自动打印日志，并生成 LogCode 用于日志定位。
 * 所以，前端捕获后不需要打印日志。并且调用getErrorCode获得用于日志定位的 LogCode 。
 *
 * 本类是fdcore内部使用的异常，项目开发中一般不需要使用本异常。
 * 应该根据需要，继承 {@link fd.ng.core.exception.BusinessProcessException}
 * 或 {@link fd.ng.core.exception.BusinessSystemException} 创建自己项目使用的异常处理类
 *
 */
public class FrameworkRuntimeException extends BaseInternalRuntimeException {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 5001120800725262628L;

	public FrameworkRuntimeException() {}

	public FrameworkRuntimeException(String msg) {
		super(msg);
		if(AppinfoConf.LoggedExceptionFrame)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), getCause());
	}

	public FrameworkRuntimeException(Throwable cause) {
		super(cause);
		if(AppinfoConf.LoggedExceptionFrame)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	public FrameworkRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		if(AppinfoConf.LoggedExceptionFrame)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	public FrameworkRuntimeException(String msg, String loggedMessage) {
		super(msg, loggedMessage);
		if(AppinfoConf.LoggedExceptionFrame)
		logger.error(String.format("%s | %s%s | %s |", getMessage(), ERRCODE_LOGPREFIX, errorCode, loggedMessage), getCause());
	}

	public FrameworkRuntimeException(String msg, String loggedMessage, Throwable cause) {
		super(msg, loggedMessage, cause);
		if(AppinfoConf.LoggedExceptionFrame)
		logger.error(String.format("%s | %s%s | %s |", getMessage(), ERRCODE_LOGPREFIX, errorCode, loggedMessage), cause);
	}
}
