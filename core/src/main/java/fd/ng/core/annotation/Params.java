package fd.ng.core.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Params {
	Param[] value();
}
