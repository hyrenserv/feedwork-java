package fd.ng.core.annotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Params {
	Param[] value();
}
