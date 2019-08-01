package fd.ng.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于在 equals 和 hashcode 中，排除成员变量
 * {@link EqualsBuilder}, {@link HashCodeBuilder}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcludedField {
}
