package fd.ng.test.junit.rule;

import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.ParallelRule;
import fd.ng.test.junit.rules.RepeatRule;
import fd.ng.test.junit.rules.RetryRule;
import fd.ng.test.junit.rules.anno.ParallelByRule;
import fd.ng.test.junit.rules.anno.Repeat;
import fd.ng.test.junit.rules.anno.Retry;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class ThreeRuleTest extends FdBaseTestCase {
	private static int RepeatCounter = 0;
	private static int RetryCounter = 0;
	private static final List<String> listParallel = new ArrayList<>();
	private static AtomicInteger ParallelCounter = new AtomicInteger(0);

	@Rule public RetryRule _RetryRule = new RetryRule();
	@Rule public RepeatRule _RepeatRule = new RepeatRule();
	@Rule public ParallelRule _ParallelRule = new ParallelRule();

	@AfterClass
	public static void endForCheck() {
		if(RepeatCounter>0) // 启动执行了repeat6才做最终的判断
			assertThat("repeat6() Fails for " + RepeatCounter, RepeatCounter, is(6));
		if(RetryCounter>0) // 启动执行了retry3才做最终的判断
			assertThat("retry3()  Fails for " + RetryCounter, RetryCounter, is(3));

		int parallelCounter = ParallelCounter.get();
		if(parallelCounter>0) // 启动执行了parallel150才做最终的判断
			assertThat("parallel150()  Fails for " + parallelCounter, parallelCounter, is(150));
	}

	@Test
	@Retry(value = 3, showFlag = true)
	public void retry3() throws Exception {
		RetryCounter++;
		TestCaseLog.println("test retry, current RetryCounter : " + RetryCounter);
		if(RetryCounter<3)
			throw new RuntimeException("test retry, current RetryCounter : " + RetryCounter);
		else
			assertThat("retry3() Fails for " + RetryCounter, RetryCounter, is(3));
	}

	@Test
	@Repeat(value = 6, showFlag = true)
	public void repeat6() throws Exception {
		RepeatCounter++;
		TestCaseLog.println("test repeat 6, current RepeatCounter : " + RepeatCounter);
		assertThat("repeat6() Fails for " + RepeatCounter, RepeatCounter, lessThan(7));
	}

	@Ignore("需要人工执行，观察是否出现了ConcurrentModificationException异常")
	@Test
	@ParallelByRule(30) // 并行数要多一些，否则不能产生：ConcurrentModificationException
	public void parallel() {
		// 如果是多线程并行执行，那么会出现对 listParallel 的同时读写，从而导致 ConcurrentModificationException
		final StringBuilder concatenation = new StringBuilder();
		for(final String str: listParallel){
			concatenation.append(str);
		}
		listParallel.add("abc");
		TestCaseLog.println("parallel concatenation=[%s], listParallel=%s",
				concatenation.toString(), listParallel.stream().collect(Collectors.joining(",")));
	}

	@Test
	@ParallelByRule(150)
	public void parallel150() {
		int curCounter = ParallelCounter.incrementAndGet();
		// 看日志，应该出现 counter 的混乱顺序
		TestCaseLog.println("parallel counter=%d", curCounter);
	}
}
