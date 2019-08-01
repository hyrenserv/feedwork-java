package fd.ng.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the fields to get the summary instead of the detailed
 * information when using {@link ReflectionToStringBuilder}.
 *
 * <p>
 * Notice that not all {@link ToStringStyle} implementations support the
 * appendSummary method.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ToStringSummary {

}
