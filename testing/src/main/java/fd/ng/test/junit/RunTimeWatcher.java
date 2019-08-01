package fd.ng.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * 使用方式是在每个Test程序中增加下面两行：
 * @Rule
 * public RunTimeWatcher G_RunTimeWatcher = new RunTimeWatcher();
 */
public class RunTimeWatcher implements TestRule {
	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				long startTime = System.currentTimeMillis();
				base.evaluate();
				System.out.printf("[ %s.%s( ) ] deal time : %dms %n",
						description.getClassName(), description.getMethodName(), (System.currentTimeMillis()-startTime));
			}
		};
	}
}
