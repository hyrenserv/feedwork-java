package fd.ng.core.exception;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RuntimeOnlyMessageException;
import fd.ng.core.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 在业务处理代码中，当出现了非正常状况，需要中断处理流程时，带着需要反馈给前端的提示信息构造本异常对象并抛出。
 * 本异常不支持对其他异常的再包装！！！！！
 * 由于重载了 fillInStackTrace ，所以本异常性能优异可随便使用，但是，没有异常堆栈！
 *
 * 名词解释：
 * 1）“非正常状况”
 *      比如：多表更新中途出现了不正确的情况，需要回滚并把错误提示信息返回前端
 *      比如：检测到了不合规的数据（如 XX数据已经存在，不能继续后续处理等），需要把错误提示信息返回前端
 *      ......
 *      如果代码中使用了 try...catch ，在 try 里面抛出了本异常，
 *      那么，在 catch 中，需要使用 instanceof 明确判断异常的类型，并分别处理：
 *      (1) 是本类异常，直接抛出
 *      (2) 不是本类异常，使用 {@link BusinessSystemException} 包裹这个异常对象后再抛出
 *
 * 2）“业务处理代码”
 *      指的就是各种网上文章中喜欢搞出来的诸如：Service层、Dao层、Controller层等等这些鸟蛋层们。
 *
 * 支持两种构建方式：
 * 1）直接使用写死的提示信息构造并抛出
 * 2）使用来自于i18n文件中的信息key名字和占位参数值
 *
 * 每个项目中，可以继承本类，重载构造函数，并且在构造函数中直接写日志，方便进行信息跟踪。
 * 比如，在WEB项目中，重载的样例代码：
 * public YourBusinessException(final String message) {
 *     super(message);
 *     logger.info("{} {}", HttpDataHolder.getBizId(), message);
 * }
 */
public class BusinessProcessException extends RuntimeOnlyMessageException {
	private static final Logger logger = LogManager.getLogger(BusinessProcessException.class.getName());
	private static final long serialVersionUID = -6070720704417179977L;
	private static final String[] EMPTY_MSGARGS = new String[0];

	protected String message;
	protected String resourceKeyName;    // 信息文件中，每行的keyname
	protected Object[] resourceArgs;  // 替换占位符的信息
	/**
	 * 错误信息的i18n ResourceBundle.
	 */
	private static final ResourceBundle rb;
	static {
		// 读取资源属性文件（properties），然后根据.properties文件的名称信息（本地化信息），匹配当前系统的国别语言信息（也可以程序指定），然后获取相应的properties文件的内容。
		// properties文件命名规范是： 自定义名_语言代码_国别代码.properties
		// 当在中文操作系统下，如果xxx_zh_CN.properties、xxx.properties两个文件都存在，则优先会使用xxx_zh_CN.properties
		// 资源文件都必须是ISO-8859-1编码。对于非西方语言，都必须先将之转换为Java Unicode Escape格式（使用 native2ascii）
		ResourceBundle tRB;
		try {
//			Locale locale1 = new Locale("zh", "CN");
//			tRB = ResourceBundle.getBundle("i18n/biz_errors", locale1);
			tRB = ResourceBundle.getBundle("i18n/biz_errors");
		} catch (Exception e) { // 这是为了，如果没有提供i18n资源文件，程序也能运转起来
			tRB = null;
			logger.error(e);
		}
		rb = tRB;
	}

	// 本异常用于中断业务处理流程的场景，所以，必须有明确的提示信息，用于反馈给前端调用业务处理方法的用户
	// 因此，不提供空参数的默认构造函数，也同时禁止了子类创建这样的构造函数
//	public BusinessProcessException() {}

	/**
	 * 直接设置错误提示信息
	 *
	 * @param message
	 *            String 错误信息
	 */
	public BusinessProcessException(final String message) {
		if(StringUtil.isEmpty(message)) throw new FrameworkRuntimeException("BusinessProcessException's arguments must not null");
		this.message = message;
	}

	/**
	 * 通过信息代码构造完整的提示信息。<Br>
	 *
	 * 例如：biz_errors.properties文件中：
	 *     ws01.project.exist=id={0}, 小区[{1}]的 {2} 的工程决案已经存在！
	 * 那么，使用方式为：
	 * new BusinessProcessException("ws01.project.exist", new Object[]{
	 *      320001, "天山花园一期", "《32~35外墙粉刷项目》"
	 * });
	 * 通过getMessage()将得到：
	 *      id=320001, 小区[天山花园一期]的 《32~35外墙粉刷项目》 的工程决案已经存在！
	 *
	 * @param resKeyName
	 *            String 代码
	 * @param resArgs
	 *            String[] 多个错误提示。因为配置文件中的信息语句中可能存在多个占位参数。
	 */
	public BusinessProcessException(final String resKeyName, final Object[] resArgs) {
		if(StringUtil.isEmpty(resKeyName)) throw new FrameworkRuntimeException("BusinessProcessException's resource KeyName must not null");
		this.resourceKeyName = resKeyName;
		if(resArgs==null) this.resourceArgs = EMPTY_MSGARGS;
		else this.resourceArgs = resArgs;
	}

	@Override
	public String getMessage() {
		if(this.message!=null) { // 这个异常对象是通过 BusinessProcessException(final String message) 构造出来的，不需要去配置文件中提起数据，直接返回即可
			return this.message;
		}
		if(rb==null) return null;
		try {
			String msg = rb.getString(resourceKeyName);
			return MessageFormat.format(msg, resourceArgs);
		} catch(MissingResourceException mse) {
			return String.format("Missing resource KeyName : '%s' for resource Args : ' %s '", resourceKeyName, Arrays.toString(resourceArgs));
		}
	}

	public String getResourceKeyName() {
		return resourceKeyName;
	}

	public Object[] getResourceArgs() {
		return resourceArgs;
	}

}
