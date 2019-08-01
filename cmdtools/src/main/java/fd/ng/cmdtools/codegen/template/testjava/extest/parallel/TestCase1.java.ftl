package ${basePackage}.${subPackage};

import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.Repeat;
import org.junit.*;

import java.util.Random;

// "仅用于 ParallelRunnerTest 的手工运行"
public class TestCase1 extends FdBaseTestCase {
	private static final int UAT_ID = 1;

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
	@Repeat(value = 3, showFlag = true)
	public void test1() {
		TestCaseLog.println("test1 in [" + UAT_ID + "] start");
		for(int i=0; i<10; i++) {
			try {
				TestCaseLog.println("test1 in [" + UAT_ID + "] running ... ... " + i);
				Random rand = new Random(System.nanoTime() + i);
				Thread.sleep(rand.nextInt(155) + 5);
			} catch (InterruptedException e) {
			}
		}
		TestCaseLog.println("test1 in [" + UAT_ID + "] end");
	}

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
