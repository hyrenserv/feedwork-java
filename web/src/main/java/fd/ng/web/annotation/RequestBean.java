package fd.ng.web.annotation;

import java.lang.annotation.*;

/**
 * 用于标注参数是否为Bean，以便自动装配该参数的值
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBean {
}
