package fd.ng.core.utils;

import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.RunTimeWatcher;
import fd.ng.test.junit.TestCaseLog;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class DateUtilTest extends FdBaseTestCase {
	@Rule public RunTimeWatcher runTimeWatcher = new RunTimeWatcher();
	@Rule public TestName testName = new TestName();

	@Ignore
	@Test
	public void testAnything() {
		long ms = System.currentTimeMillis();
		Clock ms2 = Clock.systemUTC();
		Instant instant = Instant.now();
		LocalDateTime dateTime = LocalDateTime.now();

		TestCaseLog.println("currentTimeMillis=%d, Clock.systemUTC=%d", ms, ms2.millis());
		TestCaseLog.println("instant=%s, EpochSecond=%d, getNano=%d, toEpochMilli=%d", instant, instant.toEpochMilli(),
				instant.getEpochSecond(), instant.getNano());
		TestCaseLog.println("NANO_OF_SECOND=%d, MICRO_OF_SECOND=%d, MILLI_OF_SECOND=%d, INSTANT_SECONDS=%d",
				instant.getLong(ChronoField.NANO_OF_SECOND),
				instant.getLong(ChronoField.MICRO_OF_SECOND),
				instant.getLong(ChronoField.MILLI_OF_SECOND),
				instant.getLong(ChronoField.INSTANT_SECONDS));
		TestCaseLog.println("instant=%s, dateTime=%s", instant, dateTime);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYYMMddHHmmssSSS");
		TestCaseLog.println(dateTime.format(dtf));

		StopWatch stopWatch = new StopWatch();
		int nums = 100000;
		stopWatch.start("run : " + nums);
		for (int i=0; i<nums; i++) {
//			LocalDateTime tmpDateTime = LocalDateTime.now();
			Instant tmpInstant = Instant.now();
			//String s = tmpDateTime.format(dtf);
		}
		stopWatch.stopShowSystemOut();
	}

	@Ignore
	@Test
	public void getDatetimes() {
		TestCaseLog.println("dateTime=%s", LocalDateTime.now());
		TestCaseLog.println("getSysDate=" + DateUtil.getSysDate());
		TestCaseLog.println("getSysTime=" + DateUtil.getSysTime());
		TestCaseLog.println("getDateTime=" + DateUtil.getDateTime());
		TestCaseLog.println("getTimestamp=" + DateUtil.getTimestamp());
	}
}
