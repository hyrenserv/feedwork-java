package fd.ng.test.junit.rules;

import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.Repeat;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Rule是方法级别的，每个测试方法执行时都会调用被注解的Rule。
 *
 * 被注解的方法，会反复执行多次。
 */
public class RepeatRule extends AbstractRule implements TestRule {
	@Override
	public Statement apply(final Statement statement, final Description description) {
		final String name = getAppliedMethodName(description);
		Statement result = statement;

		final Repeat repeat = description.getAnnotation(Repeat.class);
		if (repeat != null) {
			final int times = repeat.value();
			boolean showRunIndex = repeat.showFlag();
			result = new RepeatStatement(name, times, showRunIndex, result);
		}

		return result;
	}

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
}
