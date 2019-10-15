package fd.ng.core.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Class {
    String desc();//类描述
    String author() default StringUtil.EMPTY;;//作者
    String createdate() default StringUtil.EMPTY;//创建时间
}
