package fd.ng.core.utils;

import java.util.UUID;

public class UuidUtil {
	private static final long STARTED_NANO_TIME = System.nanoTime();

	public UuidUtil() { throw new AssertionError("No UuidUtil instances for you!"); }

	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", StringUtil.EMPTY); //.toLowerCase();
	}

	/**
	 * 用当前线程 id 和 当前毫秒时间作为UUID。对于WEB类应用，基本满足要求了。
	 * 但是，不能在循环调用！
	 * @return
	 */
	public static String threadId_Millis() { return Thread.currentThread().getId() + "-" + System.currentTimeMillis(); }

	public static long nanoTime() {
		return System.nanoTime();
	}
	public static long elapsedNanoTime() {
//		long cur = System.nanoTime();
//		System.out.printf("%l, %l", STARTED_NANO_TIME, cur);
//		return cur - STARTED_NANO_TIME;
		return System.nanoTime() - STARTED_NANO_TIME;
	}
	public static long timeMillis() { return System.currentTimeMillis(); }
}
