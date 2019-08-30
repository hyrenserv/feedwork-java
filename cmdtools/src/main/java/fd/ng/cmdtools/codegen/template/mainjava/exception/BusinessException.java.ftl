package ${basePackage}.${subPackage};

import fd.ng.core.exception.BusinessProcessException;
import fd.ng.web.util.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class ${className} extends BusinessProcessException {
	private static final long serialVersionUID = 8714959098642865641L;
	private static final Logger logger = LogManager.getLogger(${className}.class.getName());

	/**
	 * 直接设置错误提示信息，并自动把 message 打入日志中。<Br>
	 *
	 * @param message
	 *            String 提示信息
	 */
	public ${className}(final String message)
	{
		super(message);
		logger.info("{} {}", RequestUtil.getBizId(), getMessage());
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
	public ${className}(final String resKeyName, final Object[] resArgs)
	{
		super(resKeyName, resArgs);
		logger.info("{} {}", RequestUtil.getBizId(), getMessage());
	}

	/**
	 * 构建带有自定义业务意义编码和指定错误信息的异常，并自动把 message 打入日志中
	 *
	 * @param code int 业务层用户自定义的编码
	 * @param message String 指定错误信息
	 */
	public BusinessException(final int code, final String message) {
		super(code, message);
		logger.info("{} --> {}, {}", RequestUtil.getBizId(), code, getMessage());
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
		logger.info("{} --> {}, {}", RequestUtil.getBizId(), code, getMessage());
	}
}
