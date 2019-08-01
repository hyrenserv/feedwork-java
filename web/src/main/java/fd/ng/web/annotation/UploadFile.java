package fd.ng.web.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UploadFile {
	String savedDir() default StringUtil.EMPTY;
}
