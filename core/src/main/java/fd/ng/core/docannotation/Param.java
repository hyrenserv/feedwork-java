package fd.ng.core.docannotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Param {
	/**
	 * 参数名字，写每个方法接收的参数名称
	 * @return 返回参数名称
	 */
	String name();
	/**
	 * 中文介绍，如name="name",intro="用户名"
	 * @return
	 */
	String intro();
	/**
	 * 参数的范围，使用文本描述"如：任意，4位数字，1-100之间的数字"
	 * @return
	 */
	String range();
	/**
	 * 例子数据，如10.78.90.22这样有特殊意义的数据
	 * @return
	 */
	String example() default StringUtil.EMPTY;
}
