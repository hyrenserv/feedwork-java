package fd.ng.test.junit;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore("需要手工执行来观察验证")
public class TestCaseLogTest {

	@Test
	public void println() {
		TestCaseLog.println();
		TestCaseLog.println("test log");
	}
}