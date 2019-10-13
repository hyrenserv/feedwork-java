package fd.ng.core.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Method {
	String desc();//类描述
	String logicStep();//用 1. 2. 3. 的方式，描述方法的处理逻辑
	String example() default StringUtil.EMPTY;//例子程序
}
