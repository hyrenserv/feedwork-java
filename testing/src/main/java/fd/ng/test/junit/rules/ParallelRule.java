package fd.ng.test.junit.rules;

import fd.ng.test.junit.rules.anno.ParallelByRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Rule是方法级别的，每个测试方法执行时都会调用被注解的Rule。
 *
 * 被注解的方法，会同时并行执行。
 */
public class ParallelRule extends AbstractRule implements TestRule {
	@Override
	public Statement apply(final Statement statement, final Description description) {
		final String name = getAppliedMethodName(description);
		Statement result = statement;

		final ParallelByRule parallel = description.getAnnotation(ParallelByRule.class);
		if (parallel != null) {
			final int times = parallel.value();
			final long timeout = parallel.timeout();
			result = new ParallelStatement(name, times, timeout, result);
		}

		return result;
	}

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
