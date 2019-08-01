package fd.ng.test.junit.stmt;

import fd.ng.test.junit.TestCaseLog;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RepeatProcessor extends Statement {
	private final Statement next;
	private final FrameworkMethod method;
	private final int times;
	private final boolean showFlag;

	public RepeatProcessor(Statement next, FrameworkMethod method, int times, boolean showFlag) {
		this.next = next;
		this.times = times;
		this.method = method;
		this.showFlag = showFlag;
	}

	@Override
	public void evaluate() throws Throwable {
		String name = method.getName();
		long thid = Thread.currentThread().getId();
		if(showFlag) {
			TestCaseLog.println();
			TestCaseLog.println("======================> %s() thread_id=%d Repeat Running Start ...", name, thid);
		}
		for (int i = 0; i < times; i++) {
			if(showFlag) TestCaseLog.println("thread_id=%d ----------> Repeat %d", thid, (i+1));
			next.evaluate();
		}
		if(showFlag) {
			TestCaseLog.println("======================> %s() thread_id=%d Repeat Running Done.", name, thid);
			TestCaseLog.println();
		}
	}
}
