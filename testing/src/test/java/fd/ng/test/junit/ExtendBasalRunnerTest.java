package fd.ng.test.junit;

import fd.ng.test.junit.rules.anno.Parallel;
import fd.ng.test.junit.rules.anno.Repeat;
import fd.ng.test.junit.rules.anno.Retry;
import fd.ng.test.junit.rules.anno.Timeout;
import fd.ng.test.junit.stmt.TimeoutProcessor;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(ExtendBasalRunner.class)
public class ExtendBasalRunnerTest extends FdBaseTestCase {
	private static final String UAT_ID = "extBasalRunner";
	private int count = 0;

	@BeforeClass
	public static void start() {
		TestCaseLog.println("===== start [" + UAT_ID + "] =====\n");
	}
	@Before
	public void before() {
		TestCaseLog.println("before in [" + UAT_ID + "]");
	}
	@After
	public void after() {
		TestCaseLog.println("after in [" + UAT_ID + "]\n");
	}
	@AfterClass
	public static void end() {
		TestCaseLog.println("===== end [" + UAT_ID + "] =====\n");
	}

	@Test
	@Parallel
	public void testParallel() throws InterruptedException {
//		G_ExpectedEx.expect(TimeoutProcessor.MethodTimeoutException.class);
		Random rand = new Random(System.nanoTime());
		int runtime = rand.nextInt(500) + 5;
		Thread.sleep(runtime);
		TestCaseLog.println("testParallel in thread [ %d ], runtime is %d, in [ %s ]", Thread.currentThread().getId(), runtime, UAT_ID);
	}

	@Test
	@Parallel
	@Repeat(value = 3, showFlag = true)
	public void testParallelRepeat() throws InterruptedException {
//		G_ExpectedEx.expect(TimeoutProcessor.MethodTimeoutException.class);
		Random rand = new Random(System.nanoTime());
		int runtime = rand.nextInt(500) + 5;
		Thread.sleep(runtime);
		TestCaseLog.println("testParallelRepeat in thread [ %d ], runtime is %d, in [ %s ]", Thread.currentThread().getId(), runtime, UAT_ID);
	}

	@Test
	@Timeout(15)
	public void testTimeout() throws InterruptedException {
		G_ExpectedEx.expect(TimeoutProcessor.MethodTimeoutException.class);
		Thread.sleep(100);
		TestCaseLog.println("testTimeout OK in [ %s ]", UAT_ID);
	}

	@Test
	@Timeout(value = 100, showFlag = true)
	@Retry(value = 10, showFlag = true)
	public void testTimeoutRetry() throws InterruptedException {
		Random rand = new Random(System.nanoTime());
		Thread.sleep(rand.nextInt(20) + 92);
		TestCaseLog.println("testTimeoutRetry OK in [ %s ]", UAT_ID);
	}

	@Test
	@Repeat(value = 3)
	public void testRepeat() {
		count++;
		TestCaseLog.println("testRepeat in thread [ %d ] count=%d in [ %s ]", Thread.currentThread().getId(), count, UAT_ID);
	}

	@Test
	@Retry(5)
	@Repeat(showFlag = true)
	public void testRetryRepeat() {
		count++;
		TestCaseLog.println("testRetry count=%d in [ %s ]", count, UAT_ID);
		if(count<3) throw new RuntimeException("error");
		TestCaseLog.println("testRetry count=%d Done in [ %s ]", count, UAT_ID);
	}

	@Ignore
	@Test
	public void test2() {
		TestCaseLog.println("test2 in [" + UAT_ID + "] start");
		for(int i=0; i<10; i++) {
			try {
				TestCaseLog.println("test2 in [" + UAT_ID + "] running ... ... " + i);
				Random rand = new Random(System.nanoTime() + i);
				Thread.sleep(rand.nextInt(10) + 3);
			} catch (InterruptedException e) {
			}
		}
		TestCaseLog.println("test2 in [" + UAT_ID + "] end");
	}

	@Ignore
	@Test
	public void test3() {
		TestCaseLog.println("test3 in [" + UAT_ID + "] start");
		for(int i=0; i<10; i++) {
			try {
				TestCaseLog.println("test3 in [" + UAT_ID + "] running ... ... " + i);
				Random rand = new Random(System.nanoTime() + i);
				Thread.sleep(rand.nextInt(55) + 2);
			} catch (InterruptedException e) {
			}
		}
		TestCaseLog.println("test3 in [" + UAT_ID + "] end");
	}

}
