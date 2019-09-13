package fd.ng.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NumberUtil {
	private static final Logger logger = LogManager.getLogger(StringUtil.class.getName());

	public static final char NEGETIVE   = '-'; // 负数符号
	public static final char DEC_POINT  = '.'; // 小数点

	// ?:0或1个, *:0或多个, +:1或多个。\d判断的不只是0-9,而是一个 Unicode 字符集
	// 用正则慢5倍
	public static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");
	public static final Pattern FLOAT_PATTERN   = Pattern.compile("[0-9]+\\.[0-9]+");

	public NumberUtil() { throw new AssertionError("No NumberUtil instances for you!"); }

	/**
	 * 判断一个字符串是否为：正负数。
	 *
	 * @param numberStr
	 * @return boolean
	 */
	public static boolean isNumberic(final String numberStr) {
		return _isNumberic(numberStr, true, true, false);
	}

	/**
	 * 判断一个字符串是否为：正数。
	 *
	 * @param numberStr
	 * @return boolean
	 */
	public static boolean isPositivNumberic(final String numberStr) {
		return _isNumberic(numberStr, true, false, false);
	}

	/**
	 * 判断一个字符串是否为：正负浮点数。
	 *
	 * @param numberStr
	 * @return boolean
	 */
	public static boolean isFloat(final String numberStr) {
		return _isNumberic(numberStr, true, true, true);
	}

	/**
	 * 判断一个字符串是否为：正浮点数。
	 *
	 * @param numberStr
	 * @return boolean
	 */
	public static boolean isPositiveFloat(final String numberStr) {
		return _isNumberic(numberStr, true, false, true);
	}

	/**
	 * 判断一个字符串是否为：整数。
	 *
	 * @param integerStr
	 * @return boolean
	 */
	public static boolean isInteger(final String integerStr) {
		return _isNumberic(integerStr, false, true, false);
	}

	/**
	 * 判断一个字符串是否为：正整数。
	 *
	 * @param integerStr
	 * @return boolean
	 */
	public static boolean isPositiveInteger(final String integerStr) {
		return _isNumberic(integerStr, false, false, false);
	}

	/**
	 *
	 * @param cs
	 * @param permitPoint     允许有小数点
	 * @param permitNegative  允许有负号
	 * @param mustExistPoint  必须有小数点
	 * @return
	 */
	public static boolean _isNumberic(final CharSequence cs,
	                                  final boolean permitPoint, final boolean permitNegative,
	                                  final boolean mustExistPoint) {
		if(cs==null) return false;
		final int len = cs.length();
		if(len<1) return false;

		int index = 0;
		if(cs.charAt(0)==NEGETIVE) {    // 负号开头
			if(len==1) return false;    // 仅仅是一个符号
			if(permitNegative) {        // 允许是负数
				if(len==2) {            // 这是一个负整数
					final char ch = cs.charAt(1);
					if(ch=='0') return false;   // -0
					if(mustExistPoint) return false;
					if(isDigit(ch)) return true;
					else return false;
				} else {
					if(!isDigit(cs.charAt(1))) return false; // 符号后面必须跟着一个数字
				}
				index = 1;
			}
			else return false;
		}

		if(permitPoint) {
			if(cs.charAt(index)=='.') return false; // 以 . 开头
			if(cs.charAt(len-1)=='.') return false; // 以 . 结尾
			int pointNum = 0;
			while (index < len) {
				final char ch = cs.charAt(index);
				if(isDigit(ch)) index++;
				else if(ch==DEC_POINT) {  index++;  pointNum++; }
				else return false;
			}
			if(pointNum>1) return false;
			if(mustExistPoint&&pointNum==0) return false;
		} else {
//			return INTEGER_PATTERN.matcher(cs).matches();
			while (index < len) {
				final char ch = cs.charAt(index);
				if(isDigit(ch)) index++;
				else return false;
			}
		}
		return true;
	}

	/**
	 * 不能使用 Character.isDigit() 。因为它支持 Unicode 码！
	 * @param ch
	 * @return
	 */
	private static boolean isDigit(char ch) {
		return ch>='0' && ch<='9';
	}
}
