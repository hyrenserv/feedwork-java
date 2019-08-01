package fd.ng.core.utils;

public class ExceptionUtil {
	public ExceptionUtil() { throw new AssertionError("No ExceptionUtil instances for you!"); }
	/**
	 * from Netty, 为静态异常设置StackTrace.
	 *
	 * 对某些已知且经常抛出的异常, 不需要每次创建异常类并很消耗性能的并生成完整的StackTrace。
	 * 此时可使用静态声明的异常.
	 *
	 * 如果异常可能在多个地方抛出，使用本函数设置抛出的类名和方法名.
	 *
	 * <pre>
	 * private static RuntimeException TIMEOUT_EXCEPTION = ExceptionUtil.setStackTrace(new RuntimeException("Timeout"),
	 * 		MyClass.class, "mymethod");
	 * </pre>
	 */
	public static <T extends Throwable> T setStackTrace(T throwable, Class<?> throwClass, String throwClazz) {
		throwable.setStackTrace(
				new StackTraceElement[] { new StackTraceElement(throwClass.getName(), throwClazz, null, -1) });
		return throwable;
	}

	public static String getExceptionStack(Throwable e){
		if(e==null) return StringUtil.EMPTY;

		StringBuilder sb = new StringBuilder(160);
		StackTraceElement[] steArr = e.getStackTrace();
		for(StackTraceElement ste : steArr){
			sb.append(ste.toString()).append('\n');
		}
		return sb.toString();
	}
}
