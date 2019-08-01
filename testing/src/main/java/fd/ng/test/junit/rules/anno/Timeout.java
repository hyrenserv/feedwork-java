package fd.ng.test.junit.rules.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 注解一个测试方法可以反复重试。比如网络不通的反复重试等。
 * 如果测试用例使用了 @RunWith(ExtendBasalRunner.class) ，则只会执行一次 before/after
 * 如果测试用例使用了 @Rule public RetryRule _RetryRule = new RetryRule(); 则每次执行都会执行一次 before/after
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface Timeout {

	int DEFAULT_TIMEOUT = 1000;

	int value() default DEFAULT_TIMEOUT;
	boolean showFlag() default false; // 是否显示运行时间
}
