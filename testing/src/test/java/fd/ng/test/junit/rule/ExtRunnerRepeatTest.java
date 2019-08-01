package fd.ng.test.junit.rule;

import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.RepeatRule;
import fd.ng.test.junit.rules.anno.Repeat;
import fd.ng.test.junit.rules.ExtRunnerRules;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@Ignore("ExtRunnerRules 已经不推荐使用了")
public class ExtRunnerRepeatTest {
	private static int counter1 = 0;
	private static int counter2 = 0;
	private static int counter3 = 0;

	@Rule
	public ExtRunnerRules extRunnerRules = new ExtRunnerRules();

	@Test
	@Repeat()
	public void test1() throws Exception {
		counter1++;
		TestCaseLog.println("test1 counter1 : " + counter1);
		assertThat("Fails for " + counter1, counter1, lessThan(11));
	}

	@Test()
	@Repeat(6)
	public void test2() throws Exception {
		counter2++;
		TestCaseLog.println("test2 counter2 : " + counter2);
		assertThat("Fails for " + counter2, counter2, lessThan(7));
	}
}
