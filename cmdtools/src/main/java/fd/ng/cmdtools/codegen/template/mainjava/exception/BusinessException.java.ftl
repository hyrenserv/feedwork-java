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
}
