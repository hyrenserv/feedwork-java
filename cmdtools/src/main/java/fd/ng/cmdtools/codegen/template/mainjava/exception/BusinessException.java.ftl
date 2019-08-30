package ${basePackage}.${subPackage};

import fd.ng.core.exception.BusinessProcessException;
import fd.ng.web.util.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 在业务处理代码中，当出现了“非正常状况”，需要中断处理流程时，带着需要反馈给前端的提示信息构造本异常对象并抛出。
 *
 * 本异常处理类不支持对其他异常的再包装！！！！！
 *
 * 本异常处理类性能优异，但是，没有异常堆栈！
 *
 * “非正常状况”指的是：
 *      比如：多表更新中途出现了不正确的情况，需要回滚并把错误提示信息返回前端
 *      比如：检测到了不合规的数据（如 XX数据已经存在，不能继续后续处理等），需要把错误提示信息返回前端
 *      ......
 *
 *      如果代码中使用了 try...catch ，在 try 里面抛出了本异常，
 *      那么，在 catch 中，需要使用 instanceof 明确判断异常的类型，并分别处理：
 *      (1) 是本类异常，直接抛出
 *      (2) 不是本类异常，使用 {@link AppSystemException} 包裹这个异常对象后再抛出
 *
 * 支持四种构建方式：
 * 1）直接使用写死的提示信息构造
 * 2）使用数字代码和提示信息构造
 * 3）使用来自于i18n文件中的信息key名字和占位参数值构造
 * 4）使用 ExceptionMessage 接口对象构造
 *
 */
public class BusinessException extends BusinessProcessException {
	private static final long serialVersionUID = 8714959098642865641L;
	private static final Logger logger = LogManager.getLogger(BusinessException.class.getName());

	/**
	 * 直接设置错误提示信息，并自动把 message 打入日志中。<Br>
	 *
	 * @param message
	 *            String 提示信息
	 */
	public BusinessException(final String message)
	{
		super(message);
		logger.info("{} BusinessException : {}", RequestUtil.getBizId(), getMessage());
	}

	/**
	 * 通过资源文件的keyName，动态构造完整的提示信息。<Br>
	 *
	 * 例如：biz_errors.properties文件中：
	 *     ws01.project.exist=id: {0}, 小区[ {1} ]的 {2} 的工程决案已经存在！
	 * 那么，使用方式为：
	 * new BusinessException("ws01.project.exist", new Object[]{
	 *      320001, "天山花园一期", "《32~35外墙粉刷项目》"
	 * });
	 * 通过getMessage()将得到：
	 *      id: 320001, 小区[ 天山花园一期 ]的 《32~35外墙粉刷项目》 的工程决案已经存在！
	 *
	 * @param resKeyName
	 *            资源文件中的keyName
	 * @param resArgs
	 *            String[] 多个占位参数值。因为配置文件中的信息语句中可能存在多个占位参数。
	 */
	public BusinessException(final String resKeyName, final Object[] resArgs)
	{
		super(resKeyName, resArgs);
		logger.info("{} BusinessException : {}", RequestUtil.getBizId(), getMessage());
	}

	/**
	 * 构建带有自定义业务意义编码和指定错误信息的异常，并自动把 message 打入日志中
	 *
	 * @param code int 业务层用户自定义的编码
	 * @param message String 指定错误信息
	 */
	public BusinessException(final int code, final String message) {
		super(code, message);
		logger.info("{} BusinessException : {} --> {}", RequestUtil.getBizId(), code, getMessage());
	}

	/**
	 * 通过自定义业务意义编码和资源文件中的信息代码，构造异常对象，并自动把 message 打入日志中。<Br>
	 *
	 * @param code int 业务层用户自定义的编码
	 * @param resKeyName String 资源文件中的信息代码
	 * @param resArgs String[] 多个错误提示。因为配置文件中的信息语句中可能存在多个占位参数。
	 */
	public BusinessException(final int code, final String resKeyName, final Object[] resArgs) {
		super(code, resKeyName, resArgs);
		logger.info("{} BusinessException : {} --> {}", RequestUtil.getBizId(), code, getMessage());
	}

	/**
	 * 通过任意使用了 ExceptionMessage 接口的实例，构造异常对象，并自动把 message 打入日志中。<Br>
	 * 例如，定义了各种通用错误的枚举类 {@link ExceptionEnum}：
	 * throw new BusinessException(ExceptionEnum.DATA_UPDATE_ERROR);
	 *
	 * @param exMsg 任意使用了 ExceptionMessage 接口的实例。
	 */
	public BusinessException(final ExceptionMessage exMsg) {
		this(exMsg.getCode(), exMsg.getMessage());
	}
}
