package fd.ng.core.docannotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface DocReturn {
	/**
	 * 返回数据描述
	 * @return
	 */
	String description();
	/**
	 * 返回数据类型
	 * @return
	 */
	Class dataType() default String.class;
	/**
	 * 返回值的范围
	 * @return
	 */
	String range();
	/**
	 * 是否为实体bean
	 * @return
	 */
	boolean isRequestBean() default false;

}
