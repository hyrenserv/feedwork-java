package fd.ng.core.utils;

import fd.ng.test.junit.TestCaseLog;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

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

	@Ignore("观察几个内置方法的异同")
	@Test
	public void watchUUID() {
		System.out.printf("NEW  UUID=%s %n", UuidUtil.uuid());
		System.out.printf("Orgn UUID=%s %n", UUID.randomUUID().toString());

		StopWatch watch = new StopWatch();
		watch.start("ORGN");
		for(int i=0; i<100000; i++) {
			UUID.randomUUID().toString().replace("-", StringUtil.EMPTY);
		}
		watch.stopShow();

		watch.start("NEW ");
		for(int i=0; i<100000; i++) {
			UuidUtil.uuid();
		}
		watch.stopShow();
	}

	@Test
	public void uuid() {
		UUID uuid = UUID.randomUUID();
		for(int i=0; i<230000; i++) {
			String orgnUUID = uuid.toString().replace("-", StringUtil.EMPTY);
			String newUUID  = UuidUtil.uuid(uuid);
			assertThat(orgnUUID, is(newUUID));
		}
	}
}