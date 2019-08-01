package fd.ng.test.junit.rules;

import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.ParallelByRule;
import fd.ng.test.junit.rules.anno.Repeat;
import fd.ng.test.junit.rules.anno.Retry;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * JUnit 4 test rule that enables multiple test executions. Using provided annotations: @{@link Retry}, @{@link Repeat}
 * and @{@link ParallelByRule}, tests can be executed respectively:
 *
 * <ul>
 * <li> until success or maximum execution number executions reached with @{@link Retry} annotation </li>
 * <li> specified number of times sequentially with @{@link Repeat} annotation </li>
 * <li> specified number of times in parallel using @{@link ParallelByRule} annotation - test threads are started
 * synchronously using cyclic barrier</li>
 * </ul>
 *
 * Rule是方法级别的，每个测试方法执行时都会调用被注解的Rule。
 * 3个Rule被分开了，不需要使用这个大而全的类
 */
@Deprecated
public class ExtRunnerRules implements TestRule {

    @Override
    public Statement apply(final Statement statement, final Description description) {
        final String name = description.getClassName()+"."+description.getMethodName()+"()";
        Statement result = statement;
        final Retry retry = description.getAnnotation(Retry.class);
        if (retry != null) {
            final int times = retry.value();
            boolean showRunIndex = retry.showFlag();
            result = new RetryStatement(name, times, showRunIndex, result);
        }
        final Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            final int times = repeat.value();
            boolean showRunIndex = repeat.showFlag();
            result = new RepeatStatement(name, times, showRunIndex, result);
        }
        final ParallelByRule parallel = description.getAnnotation(ParallelByRule.class);
        if (parallel != null) {
            final int times = parallel.value();
            final long timeout = parallel.timeout();
            result = new ParallelStatement(name, times, timeout, result);
        }

        return result;
    }

    /** Wrapping statement that executes inner statement until success or retry limit reached. */
    static class RetryStatement extends Statement {
        private final String name;
        private final int times;
        private final boolean showRunIndex;
        private final Statement statement;

        RetryStatement(final String name, final int times, final boolean showRunIndex, final Statement statement) {
            this.name = name;
            this.times = times;
            this.showRunIndex = showRunIndex;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            if(showRunIndex) TestCaseLog.println(name + " Retry Running Start ...");
            Throwable lastError = null;
            for (int i = 0; i < times; i++) {
                try {
                    if(showRunIndex)
                        TestCaseLog.println(name + " -- Retry=" + (i+1));
                    statement.evaluate();
                    if(showRunIndex) TestCaseLog.println(name + " Retry Running Done");
                    return;
                } catch (final Throwable ex) {
                    lastError = ex;
                }
            }
            if(showRunIndex) TestCaseLog.println(name + " Retry Running Done");
            if (lastError != null) {
                throw lastError;
            }
        }
    }

    /** Wrapping statement that executes inner statement several times. */
    static class RepeatStatement extends Statement {
        private final String name;
        private final int times;
        private final boolean showRunIndex;
        private final Statement statement;

        RepeatStatement(final String name, final int times, final boolean showRunIndex, final Statement statement) {
            this.name = name;
            this.times = times;
            this.showRunIndex = showRunIndex;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            if(showRunIndex) TestCaseLog.println(name + " Repeat Running Start ...");
            for (int i = 0; i < times; i++) {
                if(showRunIndex)
                    TestCaseLog.println(name + " -- Repeat=" + (i+1));
                statement.evaluate();
            }
            if(showRunIndex) TestCaseLog.println(name + " Repeat Running Done.");
        }
    }

    /** Wrapping statement that executes inner statement several times in parallel. */
    static class ParallelStatement extends Statement {
        private final String name;
        private final int times;
        private final long timeout;
        private final Statement statement;
        private final CyclicBarrier startBarrier;
        private final ExecutorService executor;

        ParallelStatement(final String name, final int times, final long timeout, final Statement statement) {
            this.name = name;
            this.times = times;
            this.timeout = timeout;
            this.statement = statement;
            this.startBarrier = new CyclicBarrier(times);
            this.executor = Executors.newFixedThreadPool(times);
        }

        @Override
        public void evaluate() throws Throwable {
            final List<Future<Throwable>> results = new ArrayList<Future<Throwable>>(times);
            for (int i = 0; i < times; i++) {
                results.add(executor.submit(new Callable<Throwable>() {
                    @Override
                    public Throwable call() throws Exception {
                        try {
                            startBarrier.await();
                            statement.evaluate();
                            return null;
                        } catch (final Throwable t) {
                            return t;
                        }
                    }
                }));
            }
            executor.shutdown();
            if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("some task running timeout("+timeout+"ms)");
            }
            for (final Future<Throwable> result : results) {
                if (result.get() != null) {
                    throw result.get();
                }
            }
        }
    }
}
