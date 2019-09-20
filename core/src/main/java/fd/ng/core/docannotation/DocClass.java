package fd.ng.core.docannotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocClass {
	/**
	 * 类描述
	 * @return
	 */
	String describe();

	/**
	 * 作者
	 * @return
	 */
	String author();

	/**
	 * 时间
	 * @return
	 */
	String time();

	/**
	 * 开发公司
	 * @return
	 */
	String company() default "博彦泓智";
}
