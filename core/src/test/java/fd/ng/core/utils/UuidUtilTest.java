package fd.ng.core.utils;

import fd.ng.test.junit.TestCaseLog;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class UuidUtilTest {

	@Test
	public void threadId_Millis() {
		String uuid = UuidUtil.threadId_Millis();
		assertThat(uuid, startsWith(Thread.currentThread().getId()+"-"));
	}

	@Test
	public void elapsedNanoTime() {
		long uuid = UuidUtil.elapsedNanoTime();
		assertThat(uuid, Matchers.greaterThan(0L));
		TestCaseLog.println("UuidUtil.elapsedNanoTime()=" + uuid);
	}
}