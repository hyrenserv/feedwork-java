package fd.ng.test.junit.stmt;

import fd.ng.test.junit.TestCaseLog;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class TimeoutProcessor extends Statement {
	private final Statement next;
	private final FrameworkMethod method;
	private final int timeout; // 超时时间，毫秒
	private final boolean showFlag;

	public TimeoutProcessor(Statement next, FrameworkMethod method, int timeout, boolean showFlag) {
		this.next = next;
		this.timeout = timeout;
		this.method = method;
		this.showFlag = showFlag;
	}

	@Override
	public void evaluate() throws Throwable {
		String name = method.getName();
		long start = System.currentTimeMillis();
		next.evaluate();
		long end = System.currentTimeMillis();
		long et = end - start;
		if( et>=timeout ) {
			if(showFlag) TestCaseLog.println("%s() run time : %dms", name, et);
			throw new MethodTimeoutException( name + "() run time [" + et + "ms] greater than excepted [" + timeout + "ms]" );
		}
		if(showFlag) TestCaseLog.println("%s() run time : %dms", name, et);
	}

	public static class MethodTimeoutException extends RuntimeException {
		public MethodTimeoutException() {
		}

		public MethodTimeoutException(String message) {
			super(message);
		}
	}
}
