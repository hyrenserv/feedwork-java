package fd.ng.test.junit.rules;

import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.Retry;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Rule是方法级别的，每个测试方法执行时都会调用被注解的Rule。
 *
 * 被注解的方法，会反复执行，直到成功或达到最好重试次数。
 */
public class RetryRule extends AbstractRule implements TestRule {
	@Override
	public Statement apply(final Statement statement, final Description description) {
		final String name = getAppliedMethodName(description);
		Statement result = statement;

		final Retry retry = description.getAnnotation(Retry.class);
		if (retry != null) {
			final int times = retry.value();
			boolean showRunIndex = retry.showFlag();
			result = new RetryStatement(name, times, showRunIndex, result);
		}

		return result;
	}

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
}
