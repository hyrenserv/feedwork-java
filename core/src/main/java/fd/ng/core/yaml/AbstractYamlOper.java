package fd.ng.core.yaml;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractYamlOper {
	private static final String MIN_VALUE_NAME = "MIN_VALUE";
	private static final String MAX_VALUE_NAME = "MAX_VALUE";
	protected int fixInt(final String str) {
		if(MIN_VALUE_NAME.equals(str)) return Integer.MIN_VALUE;
		else if(MAX_VALUE_NAME.equals(str)) return Integer.MAX_VALUE;
		else return Integer.parseInt(str);
	}
	protected long fixLong(final String str) {
		if(MIN_VALUE_NAME.equals(str)) return Long.MIN_VALUE;
		else if(MAX_VALUE_NAME.equals(str)) return Long.MAX_VALUE;
		else return Long.parseLong(str);
	}
	protected BigDecimal fixDecimal(final String str) {
//		if(MIN_VALUE_NAME.equals(str)) return Long.MIN_VALUE;
//		else if(MAX_VALUE_NAME.equals(str)) return Long.MAX_VALUE;
//		else
			return new BigDecimal(str);
	}
	protected String fixString(final String str) {
		int len = str.length();
		if(str.charAt(0)=='"'&&str.charAt(len-1)=='"') {
			String ret = str.substring(1, len-1);
			return ((ret==null)? StringUtil.EMPTY:ret);
		}
		else return str;
	}

	protected <T extends Enum<T>> T _getEnum(Class<T> enumClass, String orgnValue) {
		try {
			return Enum.valueOf(enumClass, orgnValue);
		} catch (IllegalArgumentException e) {
			List<String> enumNames = new ArrayList<>();
			Enum[] enumConstants = enumClass.getEnumConstants();
			if (enumConstants != null) {
				for (Enum enumConstant : enumConstants) {
					enumNames.add(enumConstant.name());
				}
			}
			throw new FrameworkRuntimeException(
					String.format("The enum class %s create failed by value : [ %s ] (should be one of %s.)",
							enumClass.getSimpleName(), orgnValue, enumNames));
		}
	}
}
