package fd.ng.core.utils;

import java.io.StringWriter;

public class CsvUtil {
	public static final char DELIMITER              = ',';
	public static final char QUOTE                  = '"';
	public static final String QUOTE_STR            = String.valueOf(QUOTE);
	public static final String ESCAPED_QUOTE_STR    = QUOTE_STR + QUOTE_STR;

	public CsvUtil() { throw new AssertionError("No CsvUtil instances for you!"); }

	/**
	 * 转成csv字符串
	 *
	 * @param str 原始字符串
	 * @return csv格式字符串
	 */
	public static String toCsv(final String str) {
		return toCsv(str, new StringBuilder());
	}

	/**
	 * 在大循环中反复构建CSV串的时候，为提高性能，应该在循环外面创建csvBuffer为： StringBuilder csvBuffer = new StringBuilder("\"\"");
	 * 这样做是否真的提高了性能，待验证！所以，这个函数还有修改成 return 前对 csvBuffer 做删除的逻辑
	 *
	 * @param str 原始字符串
	 * @param csvBuffer 外部创建的StringBuilder对象。避免在大循环中的性能损耗
	 * @return csv格式字符串
	 */
	public static String toCsv(final String str, final StringBuilder csvBuffer) {
		if(hasEscapedChar(str)==1)              // 包含了双引号
			return csvBuffer
					.append(QUOTE)
					.append(StringUtil.replace(str, QUOTE_STR, ESCAPED_QUOTE_STR))
					.append(QUOTE)
					.toString();
		else if(hasEscapedChar(str)==2)         // 仅包括分隔符或回车换行
			return csvBuffer
					.append(QUOTE)
					.append(str)
					.append(QUOTE)
					.toString();
		else
			return str;
	}

	/**
	 * 检查给定字符串中是否存在需要转义的字符
	 *
	 * @param str 给定字符串
	 * @return int 0: 没有需要转义的字符， 1: 包含了双引号， 2: 仅包括分隔符或回车换行
	 */
	public static int hasEscapedChar(final String str) {
		int ret = 0;
		final int len = str.length();
		for (int i = 0; i < len; i++) {
			if(str.charAt(i) == QUOTE) return 1;
			if (str.charAt(i) == DELIMITER || str.charAt(i) == StringUtil.CR || str.charAt(i) == StringUtil.LF) {
				ret = 2;
			}
		}
		return ret;
	}
}
