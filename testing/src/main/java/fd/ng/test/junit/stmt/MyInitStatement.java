package fd.ng.test.junit.stmt;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class MyInitStatement extends Statement {
	private final FrameworkMethod testMethod;
	private final Object target;

	public MyInitStatement(FrameworkMethod testMethod, Object target) {
		this.testMethod = testMethod;
		this.target = target;
	}

	@Override
	public void evaluate() throws Throwable {
		testMethod.invokeExplosively(target);
	}
}
