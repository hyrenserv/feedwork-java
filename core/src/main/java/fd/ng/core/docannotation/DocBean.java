package fd.ng.core.docannotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface DocBean {
	/**
	 * 实体属性名称
	 * @return
	 */
	String name();

	/**
	 * 实体属性中午描述
	 * @return
	 */
	String value();

	/**
	 * 实体属性数据类型
	 * @return
	 */
	Class dataType();

	/**
	 * 是否可以为空
	 * @return
	 */
	boolean required() default true;
}
