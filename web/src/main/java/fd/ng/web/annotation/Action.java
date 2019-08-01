package fd.ng.web.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

/**
 * 被Action注解的类，如果没有给 UriExt 赋值，则系统默认取该类的包作为URI
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {
	/**
	 * 该值会被追加到当前 Action 类的包名后面，实现一个包下多个Action的目的。
	 * @return 包名Uri的后缀名
	 */
	String UriExt() default StringUtil.EMPTY;
}
