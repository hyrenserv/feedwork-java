package fd.ng.core.exception.internal;

import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.exception.BusinessSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 系统内部使用的软件产生的异常，无论是受检（SQLException）还是不受检（NullPointerException），
 * 都封装为 RawlayerRuntimeException 抛出，这种异常需要完整的原始异常堆栈和异常信息。
 * 抛出该异常时，如果使用自己描述的信息，需使用英文，因为该异常省去了对异常信息的i18n。
 * 该异常在 new 时自动打印日志，并生成 ERRCODE 用于日志定位。
 * WEB总控捕获后，给前端提示为固定英文（System Internal Running Error）和用于日志定位的 ERRCODE 。
 *
 * 本异常为fdcore各组件内部使用的异常，在项目中，应该使用子类：{@link BusinessSystemException}
 *
 * 重要的事情多说一次：
 * 该异常在 new 时自动打印日志，并生成 ERRCODE 用于日志定位。
 * 所以，前端捕获后不需要打印日志。并且调用getErrorCode获得用于日志定位的 ERRCODE 。
 *
 * 本类是fdcore内部使用的异常，项目开发中一般不需要使用本异常。
 * 应该根据需要，继承 {@link fd.ng.core.exception.BusinessProcessException}
 * 或 {@link fd.ng.core.exception.BusinessSystemException} 创建自己项目使用的异常处理类
 *
 */
public class RawlayerRuntimeException extends BaseInternalRuntimeException {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -325737848068753531L;

	public RawlayerRuntimeException(String msg) {
		super(msg);
		if(AppinfoConf.LoggedExceptionRaw)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), getCause());
	}

	public RawlayerRuntimeException(Throwable cause) {
		super(cause);
		if(AppinfoConf.LoggedExceptionRaw)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	public RawlayerRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
		if(AppinfoConf.LoggedExceptionRaw)
		logger.error(String.format("%s | %s%s |", getMessage(), ERRCODE_LOGPREFIX, errorCode), cause);
	}

	public RawlayerRuntimeException(String canbeGettedMessage, String loggedMessage) {
		super(canbeGettedMessage, loggedMessage);
		if(AppinfoConf.LoggedExceptionRaw)
		logger.error(String.format("%s | %s%s | %s |", getMessage(), ERRCODE_LOGPREFIX, errorCode, loggedMessage), getCause());
	}

	public RawlayerRuntimeException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, loggedMessage, cause);
		if(AppinfoConf.LoggedExceptionRaw)
		logger.error(String.format("%s | %s%s | %s |", getMessage(), ERRCODE_LOGPREFIX, errorCode, loggedMessage), cause);
	}
}
