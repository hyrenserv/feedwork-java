package fd.ng.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringUtil {
	private static final Logger logger = LogManager.getLogger(StringUtil.class.getName());
	public static final String UTF_8 = "UTF-8";
	public static final String EMPTY = "";
	public static final int INDEX_NOT_FOUND = -1;
	public static final String BLANK = " ";
	public static final char LF = '\n';
	public static final char CR = '\r';
	public static final char NUL = '\0';
	public static final char AT_SIGN = '@';

	// 驼峰风格和下划线风格转换用
	public static final Pattern PATTERN_HUMP2UNDERLINE = Pattern.compile("[A-Z]");
	public static final Pattern PATTERN_UNDERLINE2HUMP = Pattern.compile("_[a-z]");

	public StringUtil() {
		throw new AssertionError("No StringUtil instances for you!");
	}

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * 判断是否空白字符串。空白字符包括：空格、tab键、换行符
	 *
	 * @param cs
	 * @return
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	/**
	 * 将驼峰风格替换为下划线风格
	 */
	public static String humpToUnderline(String str) {
		Matcher matcher = PATTERN_HUMP2UNDERLINE.matcher(str);
		StringBuilder builder = new StringBuilder(str);
		for (int i = 0; matcher.find(); i++) {
			builder.replace(matcher.start() + i, matcher.end() + i, "_" + matcher.group().toLowerCase());
		}
		if (builder.charAt(0) == '_') {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}

	/**
	 * 将下划线风格替换为驼峰风格
	 */
	public static String underlineToHump(String str) {
		Matcher matcher = PATTERN_UNDERLINE2HUMP.matcher(str);
		StringBuilder builder = new StringBuilder(str);
		for (int i = 0; matcher.find(); i++) {
			builder.replace(matcher.start() - i, matcher.end() - i, matcher.group().substring(1).toUpperCase());
		}
		if (Character.isUpperCase(builder.charAt(0))) {
			builder.replace(0, 1, String.valueOf(Character.toLowerCase(builder.charAt(0))));
		}
		return builder.toString();
	}

	/**
	 * 将字符串首字母大写
	 */
	public static String firstToUpper(String str) {
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * 将字符串首字母小写
	 */
	public static String firstToLower(String str) {
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * 得到子串出现的次数
	 *
	 * @param sourceStr 源串
	 * @param searchStr 要查找的字符串
	 * @return 出现的次数
	 */
	public static int substringCount(final String sourceStr, final String searchStr) {
		if (StringUtil.isEmpty(sourceStr) || StringUtil.isEmpty(searchStr)) return 0;
		int loc = sourceStr.indexOf(searchStr);
		if (loc < 0) return 0;
		int count = 1; // 记录出现的次数。 前面indexOf已经找到了，所以至少有1次。
		int serLen = searchStr.length();
		String subStr = sourceStr.substring(loc + serLen);
		while (true) {
			loc = subStr.indexOf(searchStr);
			if (loc < 0)
				break;
			subStr = subStr.substring(loc + serLen);
			count++;
		}
		return count;
	}

	/**
	 * 因为JDK中replaceFirst是做正则匹配的，影响性能。
	 *
	 * @param sourceStr 源串
	 * @param searchStr 要查找的字符串
	 * @return
	 */
	public static String replaceFirst(final String sourceStr, final String searchStr) {
		return replaceFirst(sourceStr, searchStr, EMPTY);
	}

	/**
	 * 从前向后，替换第一次出现的位置。
	 * 因为JDK中replaceFirst是做正则匹配的，影响性能。
	 *
	 * @param sourceStr
	 * @param searchStr
	 * @param replaceStr
	 * @return
	 */
	public static String replaceFirst(final String sourceStr, final String searchStr, final String replaceStr) {
		if (StringUtil.isEmpty(sourceStr) || StringUtil.isEmpty(searchStr)) return sourceStr;
		int firstLoc = sourceStr.indexOf(searchStr);
		if (firstLoc == 0) {
			if (StringUtil.isEmpty(replaceStr))
				return sourceStr.substring(searchStr.length());
			else
				return replaceStr + sourceStr.substring(searchStr.length());
		} else {
			int srcLen = sourceStr.length();
			int serLen = searchStr.length();
			if (StringUtil.isEmpty(replaceStr)) {
				return sourceStr.substring(0, firstLoc) + sourceStr.substring(firstLoc + serLen, srcLen);
			} else {
				int repLen = replaceStr.length();
				StringBuilder sb = new StringBuilder(srcLen - serLen + repLen);
				sb.append(sourceStr.substring(0, firstLoc))
						.append(replaceStr)
						.append(sourceStr.substring(firstLoc + serLen, srcLen));
				return sb.toString();
			}
		}
	}

	public static String replaceLast(final String sourceStr, final String searchStr) {
		return replaceLast(sourceStr, searchStr, EMPTY);
	}

	public static String replaceLast(final String sourceStr, final String searchStr, final String replaceStr) {
		if (StringUtil.isEmpty(sourceStr) || StringUtil.isEmpty(searchStr)) return sourceStr;
		if (sourceStr.endsWith(searchStr)) {
			int loc = sourceStr.lastIndexOf(searchStr);
			if (StringUtil.isEmpty(replaceStr))
				return sourceStr.substring(0, loc);
			else
				return sourceStr.substring(0, loc) + replaceStr;
		} else {
			// 如果要支持正则表达式搜索，可以用下面这句
			// return sourceStr.replaceFirst("(?s)(.*)" + searchString, "$1" + replaceString);
			int lastLoc = sourceStr.lastIndexOf(searchStr);
			if (lastLoc < 0) return sourceStr;
			else if (lastLoc == 0) { // 在开头
				if (StringUtil.isEmpty(replaceStr))
					return sourceStr.substring(searchStr.length());
				else
					return replaceStr + sourceStr.substring(searchStr.length());
			}
			// TODO: 如果被替换串只有一个，那么没必要像下面搞这么复杂，直接替换即可
//			int firstLoc = sourceStr.indexOf(searchStr);
//			if(firstLoc==lastLoc){
//				if(replaceStr==null)
//					return sourceStr.replace(searchStr, "");
//				else
//					return sourceStr.replace(searchStr, replaceStr);
//			}
			int srcLen = sourceStr.length();
			int serLen = searchStr.length();
			int repLen = replaceStr.length();
			StringBuilder sb = new StringBuilder(srcLen - serLen + repLen);
			sb.append(sourceStr.substring(0, lastLoc))
					.append(replaceStr)
					.append(sourceStr.substring(lastLoc + serLen, srcLen));
			return sb.toString();
		}
	}

	/**
	 * <p>Appends the toString that would be produced by {@code Object}
	 * if a class did not override toString itself. {@code null}
	 * will throw a NullPointerException for either of the two parameters. </p>
	 *
	 * <pre>
	 * StringUtil.identityToString(buf, "")            = buf.append("java.lang.String@1e23"
	 * StringUtil.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa"
	 * StringUtil.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
	 * </pre>
	 *
	 * @param buffer the buffer to append to
	 * @param object the object to create a toString for
	 */
	public static void identityToString(final StringBuffer buffer, final Object object) {
		Validator.notNull(object, "Cannot get the toString of a null object");
		final String name = object.getClass().getName();
		final String hexString = Integer.toHexString(System.identityHashCode(object));
		buffer.ensureCapacity(buffer.length() + name.length() + 1 + hexString.length());
		buffer.append(name)
				.append(AT_SIGN)
				.append(hexString);
	}

	/**
	 * <p>Checks that the CharSequence does not contain certain characters.</p>
	 *
	 * <p>A {@code null} CharSequence will return {@code true}.
	 * A {@code null} invalid character array will return {@code true}.
	 * An empty CharSequence (length()=0) always returns true.</p>
	 *
	 * <pre>
	 * StringUtil.containsNone(null, *)       = true
	 * StringUtil.containsNone(*, null)       = true
	 * StringUtil.containsNone("", *)         = true
	 * StringUtil.containsNone("ab", '')      = true
	 * StringUtil.containsNone("abab", 'xyz') = true
	 * StringUtil.containsNone("ab1", 'xyz')  = true
	 * StringUtil.containsNone("abz", 'xyz')  = false
	 * </pre>
	 *
	 * @param cs          the CharSequence to check, may be null
	 * @param searchChars an array of invalid chars, may be null
	 * @return true if it contains none of the invalid chars, or is null
	 */
	public static boolean containsNone(final CharSequence cs, final char... searchChars) {
		if (cs == null || searchChars == null) {
			return true;
		}
		final int csLen = cs.length();
		final int csLast = csLen - 1;
		final int searchLen = searchChars.length;
		final int searchLast = searchLen - 1;
		for (int i = 0; i < csLen; i++) {
			final char ch = cs.charAt(i);
			for (int j = 0; j < searchLen; j++) {
				if (searchChars[j] == ch) {
					if (Character.isHighSurrogate(ch)) {
						if (j == searchLast) {
							// missing low surrogate, fine, like String.indexOf(String)
							return false;
						}
						if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
							return false;
						}
					} else {
						// ch is in the Basic Multilingual Plane
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * <p>Checks if the CharSequence contains any character in the given
	 * set of characters.</p>
	 *
	 * <p>A {@code null} CharSequence will return {@code false}.
	 * A {@code null} or zero length search array will return {@code false}.</p>
	 *
	 * <pre>
	 * StringUtil.containsAny(null, *)                = false
	 * StringUtil.containsAny("", *)                  = false
	 * StringUtil.containsAny(*, null)                = false
	 * StringUtil.containsAny(*, [])                  = false
	 * StringUtil.containsAny("zzabyycdxx",['z','a']) = true
	 * StringUtil.containsAny("zzabyycdxx",['b','y']) = true
	 * StringUtil.containsAny("zzabyycdxx",['z','y']) = true
	 * StringUtil.containsAny("aba", ['z'])           = false
	 * </pre>
	 *
	 * @param cs          the CharSequence to check, may be null
	 * @param searchChars the chars to search for, may be null
	 * @return the {@code true} if any of the chars are found,
	 * {@code false} if no match or null input
	 */
	public static boolean containsAny(final CharSequence cs, final char... searchChars) {
		if (isEmpty(cs) || searchChars == null || searchChars.length == 0) {
			return false;
		}
		final int csLength = cs.length();
		final int searchLength = searchChars.length;
		final int csLast = csLength - 1;
		final int searchLast = searchLength - 1;
		for (int i = 0; i < csLength; i++) {
			final char ch = cs.charAt(i);
			for (int j = 0; j < searchLength; j++) {
				if (searchChars[j] == ch) {
					if (Character.isHighSurrogate(ch)) {
						if (j == searchLast) {
							// missing low surrogate, fine, like String.indexOf(String)
							return true;
						}
						if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
							return true;
						}
					} else {
						// ch is in the Basic Multilingual Plane
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 去掉结尾的回车换行符
	 * <pre>
	 * StringUtils.trimCrLf(null)          = null
	 * StringUtils.trimCrLf("")            = ""
	 * StringUtils.trimCrLf("abc \r")      = "abc "
	 * StringUtils.trimCrLf("abc\n")       = "abc"
	 * StringUtils.trimCrLf("abc\r\n")     = "abc"
	 * StringUtils.trimCrLf("abc\r\n\r\n") = "abc\r\n"
	 * StringUtils.trimCrLf("abc\n\r")     = "abc\n"
	 * StringUtils.trimCrLf("abc\n\rabc")  = "abc\n\rabc"
	 * StringUtils.trimCrLf("\r")          = ""
	 * StringUtils.trimCrLf("\n")          = ""
	 * StringUtils.trimCrLf("\r\n")        = ""
	 * </pre>
	 *
	 * @param str 源串
	 * @return String
	 */
	public static String trimCrLf(final String str) {
		if (isEmpty(str)) {
			return str;
		}

		if (str.length() == 1) {
			final char ch = str.charAt(0);
			if (ch == CR || ch == LF) {
				return EMPTY;
			}
			return str;
		}

		int lastIdx = str.length() - 1;
		final char last = str.charAt(lastIdx);

		if (last == LF) {
			if (str.charAt(lastIdx - 1) == CR) {
				lastIdx--;
			}
		} else if (last != CR) {
			lastIdx++;
		}
		return str.substring(0, lastIdx);
	}

	/**
	 * 如果仅仅是char的替换，JDK原生方法极快！
	 * 所以，这个方法根本不需要有，写在这里是为了提示大家，去用JDK原生方法即可。
	 *
	 * @param srcString
	 * @param searchChar
	 * @param repChar
	 * @return
	 */
	public static String replace(final String srcString, final char searchChar, final char repChar) {
		if (srcString == null) {
			return null;
		}
		return srcString.replace(searchChar, repChar);
	}

	/**
	 * 比JDK原生的replace的性能要好很多。
	 *
	 * @param srcString
	 * @param searchStr
	 * @param repStr
	 * @return
	 */
	public static String replace(final String srcString, final String searchStr, final String repStr) {
		if (isEmpty(srcString) || isEmpty(searchStr) || repStr == null) {
			return srcString;
		}
		int start = 0;
		int end = srcString.indexOf(searchStr, start);
		if (end == INDEX_NOT_FOUND) {
			return srcString;
		}
		final int repLen = searchStr.length();
		int increase = repStr.length() - repLen;
		increase = (increase > 0 ? increase * 16 : 0);
		final StringBuilder buf = new StringBuilder(srcString.length() + increase);
		while (end != INDEX_NOT_FOUND) {
			buf.append(srcString, start, end).append(repStr);
			start = end + repLen;
			end = srcString.indexOf(searchStr, start);
		}
		buf.append(srcString, start, srcString.length());
		return buf.toString();
	}

	/**
	 * 分割字符串，返回List<String>。如果想得到String[]，可对结果List做转换：list.toArray(new String[list.size()]);
	 * 因为JDK的split有各种怪异问题，比如结尾多个连续分隔符号会被忽略掉（1:2:3::::，这种串用:分隔时，只能返回1,2,3)
	 * <p>
	 * 可以使用 StringFaster 试试是不是能更快。
	 * <p>
	 * 另外，GUAVA的split超快，是因为他返回的是Iterator，实际并没有一个个分隔了串，只是在开始取数时才开始分隔
	 * 所以，如果只是取分隔的前几个子串，他最快，但是如果每个子串都取一次，就和其他方式一样的耗时了
	 *
	 * @param str       被分割的原始字符串
	 * @param separator 分割符
	 * @return List<String> 不会null，两个入参任何一个为空，都放回一个空List。
	 */
	public static List<String> split(final String str, final String separator) {
		if (str == null) {
			return Collections.emptyList();
		}
		int len = str.length();
		if (len == 0) {
			return Collections.emptyList();
		}
		if ((separator == null) || (EMPTY.equals(separator))) {
			return Collections.singletonList(str);
		}

		final int separatorLength = separator.length();

		final List<String> substrings = new ArrayList<>();
		int begin = 0;
		int end = 0;
		while (end < len) {
			end = str.indexOf(separator, begin);
			if (end > -1) {
				if (end > begin) {
					substrings.add(str.substring(begin, end));
					begin = end + separatorLength;
				} else {
					// 连续出现了分隔符
					substrings.add(EMPTY);
					begin = end + separatorLength;
				}
			} else {
				substrings.add(str.substring(begin));
				end = len;
			}
		}
		return substrings;
	}

//	public static List<String> splitFaster(final StringFaster str, final StringFaster separator) {
//		if (str == null) {
//			return Collections.emptyList();
//		}
//		int len = str.length();
//		if (len == 0) {
//			return Collections.emptyList();
//		}
//		if ((separator == null) || separator.isEmpty()) {
//			return Collections.singletonList(str.toString());
//		}
//
//		final int sepLength = separator.length();
//
//		List<String> substrings = new ArrayList<>();
//		int beggin = 0;
//		int end = 0;
//		while (end < len) {
//			end = str.indexOfFaster(separator, sepLength, beggin);
//
//			if (end > -1) {
//				if (end > beggin) {
//					substrings.add(str.substringFaster(beggin, end));
//
//					beggin = end + sepLength;
//				} else {
//					// 连续出现了分隔符
//					substrings.add(EMPTY);
//					beggin = end + sepLength;
//				}
//			} else {
//				substrings.add(str.substringFaster(beggin));
//				end = len;
//			}
//		}
//
//		return substrings;
//
//	}

	public static String lenientFormat(String template, Object... args) {
		template = String.valueOf(template); // null -> "null"

		if (args == null) {
			args = new Object[]{"(Object[])null"};
		} else {
			for (int i = 0; i < args.length; i++) {
				args[i] = lenientToString(args[i]);
			}
		}

		// start substituting the arguments into the '%s' placeholders
		StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
		int templateStart = 0;
		int i = 0;
		while (i < args.length) {
			int placeholderStart = template.indexOf("%s", templateStart);
			if (placeholderStart == -1) {
				break;
			}
			builder.append(template, templateStart, placeholderStart);
			builder.append(args[i++]);
			templateStart = placeholderStart + 2;
		}
		builder.append(template, templateStart, template.length());

		// if we run out of placeholders, append the extra args in square braces
		if (i < args.length) {
			builder.append(" [");
			builder.append(args[i++]);
			while (i < args.length) {
				builder.append(", ");
				builder.append(args[i++]);
			}
			builder.append(']');
		}

		return builder.toString();
	}

	private static String lenientToString(Object o) {
		try {
			return String.valueOf(o);
		} catch (Exception e) {
			// Default toString() behavior - see Object.toString()
			String objectToString =
					o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
			logger.warn("Exception during lenientFormat for " + objectToString, e);
			return "<" + objectToString + " threw " + e.getClass().getName() + ">";
		}
	}

	/**
	 * 字符串转换unicode
	 */
	public static String string2Unicode(String string) {
		StringBuffer unicode = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			// 取出每一个字符
			char c = string.charAt(i);
			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}
		return unicode.toString();
	}

	/**
	 * unicode 转字符串
	 */
	public static String unicode2String(String unicode) {
		StringBuffer string = new StringBuffer();
		String[] hex = unicode.split("\\\\u");
		for (int i = 1; i < hex.length; i++) {
			// 转换出每一个代码点
			int data = Integer.parseInt(hex[i], 16);
			// 追加成string
			string.append((char) data);
		}
		return string.toString();
	}
}
