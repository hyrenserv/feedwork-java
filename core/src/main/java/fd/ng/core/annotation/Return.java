package fd.ng.core.annotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Return {
	String desc();//返回数据描述
	String range();//返回值的范围
	boolean isBean() default false;//是否为实体bean

}
