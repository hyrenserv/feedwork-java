package fd.ng.web.helper;

import org.apache.logging.log4j.Logger;

/**
 * 帮助处理web层日志，如要就是自动追加上一次请求的唯一标识
 */
public class Loghelper {
	private Loghelper(){}
	/**
	 * 代码中尽量使用本函数。
	 * 用法示例：
	 * logger.debug( Loghelper.fitMessage("新添加的用户为：name=%s, age=%d", name, age) );
	 * @param msg 带格式占位符号的字符串
	 * @param args 多个参数，与占位符号匹配
	 * @return 前缀追加了唯一请求id的字符串
	 */
	public static String fitMessage(String msg, Object... args) {
		if (args.length > 0) {
			String orgnStr = String.format(msg, args);
			return String.format("%s %s",
					HttpDataHolder.getBizId(), orgnStr);
		}
		else {
			if(msg==null) return HttpDataHolder.getBizId();
			else return String.format("%s %s", HttpDataHolder.getBizId(), msg);
		}
	}

	/**
	 * 使用本函数及其下面的各个函数，会导致打印出来的日志中，调用类和方法都是本类！
	 * @param logger Logger
	 * @param msg 带格式占位符号的字符串
	 * @param args 多个参数，与占位符号匹配
	 */
	public static void debug(Logger logger, String msg, Object... args) {
		if(logger.isDebugEnabled()) {
			logger.debug(fitMessage(msg, args));
		}
	}
	public static void info(Logger logger, String msg, Object... args) {
		if(logger.isInfoEnabled()) {
			logger.info(fitMessage(msg, args));
		}
	}
	public static void warn(Logger logger, String msg, Object... args) {
		logger.warn(fitMessage(msg, args));
	}

	public static void error(Logger logger, String msg, Object... args) {
		logger.error(fitMessage(msg, args));
	}
	public static void error(Logger logger, Throwable t, String msg, Object... args) {
		logger.error(fitMessage(msg, args), t);
	}
}
