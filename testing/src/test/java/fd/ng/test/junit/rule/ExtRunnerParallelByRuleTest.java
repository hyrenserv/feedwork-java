package fd.ng.test.junit.rule;

import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.ParallelByRule;
import fd.ng.test.junit.rules.ExtRunnerRules;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Ignore("ExtRunnerRules 已经不推荐使用了")
public class ExtRunnerParallelByRuleTest {
	private static AtomicInteger counter = new AtomicInteger(0);
	private static final List<String> list1 = new ArrayList<String>();
	private static final List<String> list2 = new CopyOnWriteArrayList<String>();
	private static final List<String> list3 = new CopyOnWriteArrayList<String>();

	@Rule
	public ExtRunnerRules extRunnerRules = new ExtRunnerRules();
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Before
	public void count() {
		TestCaseLog.println("before counter : " + counter.get());
	}

	@Ignore("会抛出异常： ConcurrentModificationException。因为ArrayList不是线程安全的，读的同时被写，就会抛异常")
	@Test
	@ParallelByRule(100)
	public void test1() throws Exception {
		expectedEx.expect(ConcurrentModificationException.class);
		for(final String str: list1){
		}
		list1.add("abc");
	}

	@Test
	@ParallelByRule(5)
	public void test2() throws Exception {
		final StringBuilder concatenation = new StringBuilder();
		for(final String str: list2){
			concatenation.append(str);
		}
		list2.add("abc");
		int curCounter = counter.incrementAndGet();
		TestCaseLog.println("test2 counter=%d, concatenation=[%s], list2=%s",
				curCounter, concatenation.toString(), list2.stream().collect(Collectors.joining(",")));
	}

	@Ignore("Will fail due to timeout")
	@Test
	@ParallelByRule(timeout = 14)
	public void test3() throws Exception {
		final StringBuilder concatenation = new StringBuilder();
		for(final String str: list3){
			Thread.sleep(15);
			concatenation.append(str);
		}
		list3.add("abc");
	}
}
