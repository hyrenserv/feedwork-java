package fd.ng.test.junit.rules.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 注解一个方法被反复执行多次。
 * 如果测试用例使用了 @RunWith(ExtendBasalRunner.class) ，则只会执行一次 before/after
 * 如果测试用例使用了 @Rule public RepeatRule _RepeatRule = new RepeatRule(); 则每次执行都会执行一次 before/after
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface Repeat {

    int DEFAULT_REPETITION_COUNT = 10;

    /** Number of test repetitions. */
    int value() default DEFAULT_REPETITION_COUNT;
    boolean showFlag() default false; // 是否显示 repeat 次数
}
