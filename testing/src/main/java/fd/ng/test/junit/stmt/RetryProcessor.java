package fd.ng.test.junit.stmt;

import fd.ng.test.junit.TestCaseLog;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RetryProcessor extends Statement {
	private final Statement next;
	private final FrameworkMethod method;
	private final int times;
	private final boolean showFlag;

	public RetryProcessor(Statement next, FrameworkMethod method, int times, boolean showFlag) {
		this.next = next;
		this.times = times;
		this.method = method;
		this.showFlag = showFlag;
	}

	@Override
	public void evaluate() throws Throwable {
		String name = method.getName();
		Throwable lastError = null;
		for (int i = 0; i < times; i++) {
			try {
//				TestCaseLog.println(name + " -- Retry=" + (i+1));
				next.evaluate();
				if(showFlag) TestCaseLog.println("%s() successfully completed after %d retries!", name, (i+1));
				return;
			} catch (final Throwable ex) {
				lastError = ex;
			}
		}
		if (lastError != null) {
			throw lastError;
		} else {
			TestCaseLog.println(name + " retry %d times, No exception done, somthing wrong?");
		}
	}
}
