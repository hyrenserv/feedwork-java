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
public @interface Parallel {
	int DEFAULT_THREAD_COUNT = 10;
	int DEFAULT_TIMEOUT_MS = 30000;

	// 并行执行的范围
	int OnlyTestMethod = 1;   // 仅仅对测试方法做并行，before/after仅执行一次
	int WithBeforeAfter = 2;  // 连同before/after一起并行执行

	/** 默认并行数 */
	int value() default DEFAULT_THREAD_COUNT;

	/** 线程执行超时（毫秒） */
	long timeout() default DEFAULT_TIMEOUT_MS;

	int scope() default OnlyTestMethod; // 默认对仅对测试方法并行执行
}
