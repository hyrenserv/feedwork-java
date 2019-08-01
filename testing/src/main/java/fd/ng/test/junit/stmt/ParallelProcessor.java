package fd.ng.test.junit.stmt;

import fd.ng.test.junit.TestCaseLog;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelProcessor extends Statement {
	private final Statement next;
	private final FrameworkMethod method;
	private final int nums;
	private final long timeout;
	private final int scope;
	private final CyclicBarrier startBarrier;
	private final ExecutorService executor;

	public ParallelProcessor(Statement next, FrameworkMethod method, int nums, long timeout, int scope) {
		this.next = next;
		this.method = method;
		this.nums = nums;
		this.timeout = timeout;
		this.scope = scope;
		this.startBarrier = new CyclicBarrier(nums);
		this.executor = Executors.newFixedThreadPool(nums);
	}

	@Override
	public void evaluate() throws Throwable {
		String name = method.getName();
		final List<Future<Throwable>> results = new ArrayList<Future<Throwable>>(nums);
		for (int i = 0; i < nums; i++) {
			results.add(executor.submit(new Callable<Throwable>() {
				@Override
				public Throwable call() throws Exception {
					try {
						startBarrier.await();
						next.evaluate();
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

	public static class IllegalScopeException extends RuntimeException {
		public IllegalScopeException() {
		}

		public IllegalScopeException(String message) {
			super(message);
		}
	}
}
