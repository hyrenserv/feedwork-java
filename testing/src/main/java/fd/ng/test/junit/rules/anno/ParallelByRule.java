package fd.ng.test.junit.rules.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 注解测试用例被多线程并行执行
 * 本注解有 ParallelRule 使用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface ParallelByRule {

    int DEFAULT_THREAD_COUNT = 10;
    int DEFAULT_TIMEOUT_MS = 30000;

    /** 默认并行数 */
    int value() default DEFAULT_THREAD_COUNT;

    /** 线程执行超时（毫秒） */
    long timeout() default DEFAULT_TIMEOUT_MS;
}
