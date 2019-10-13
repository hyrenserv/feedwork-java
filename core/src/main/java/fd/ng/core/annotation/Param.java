package fd.ng.core.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD,ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Params.class)
public @interface Param {
	String name() default StringUtil.EMPTY; // 值为方法的参数名字
	String alias() default StringUtil.EMPTY; // 参数别名。比如，在WEB中，对应 request 中名字
	boolean nullable() default false; // 该参数是否可以不存在或为空串。
	String[] valueIfNull() default {}; // 对于可空变量，赋予的默认值（因为前端提交的数据有可能是字符数组）
	boolean ignore() default false; // 是否忽略（跳过）被注解的参数
	boolean isBean() default false; // 该参数是否为JavaBean

	/**
	 * 用于描述参数的含义
	 */
	String desc();

	/**
	 * 用于描述参数的取值范围，"如：任意，4位数字，1-100之间的数字"。
	 */
	String range();

	/**
	 * 例子数据，如10.78.90.22这样有特殊意义的数据
	 */
	String example() default StringUtil.EMPTY;
}