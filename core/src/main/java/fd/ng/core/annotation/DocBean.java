package fd.ng.core.annotation;


import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocBean {
	String name();//实体属性名称
	String value();// 实体属性中午描述
	Class dataType();//实体属性数据类型
	boolean required() default true;//是否可以为空
}