package fd.ng.web.hmfmswebapp.anno;

import fd.ng.core.utils.Validator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OptionalObject {
	public OptionalObject() {
		System.out.println("new OptionalObject");
	}
	public Optional<Map<String, Integer>> getValue(String value) {
		if(value==null)
			return Optional.ofNullable(null);
		else if(value.equalsIgnoreCase("error"))
			return Optional.empty();
		else {
			Map<String, Integer> map = new HashMap<>();
			map.put(value, new Integer(value));
			return Optional.of(map);
		}
	}

	public static Optional<Object> castStringToClass(String[] fromStringValue, Class<?> toClass) {
		Validator.notNull(toClass);
		if(fromStringValue==null||fromStringValue.length==0) {
			if(int.class.isAssignableFrom(toClass) || long.class.isAssignableFrom(toClass)
					|| int[].class.isAssignableFrom(toClass) || long[].class.isAssignableFrom(toClass)) { // 对于主类型，不能生成null返回。
				return null;
			}
			else
				return Optional.empty();
		}

		if (String.class.isAssignableFrom(toClass)) {
			return Optional.of(fromStringValue[0]);
		} else if(int.class.isAssignableFrom(toClass)) {
			return Optional.of(Integer.parseInt(fromStringValue[0]));
		} else if(Integer.class.isAssignableFrom(toClass)) {
			return Optional.of(new Integer(fromStringValue[0]));
		} else if(BigDecimal.class.isAssignableFrom(toClass)) {
			return Optional.of(new BigDecimal(fromStringValue[0]));
		} else if(long.class.isAssignableFrom(toClass)) {
			return Optional.of(Long.parseLong(fromStringValue[0]));
		} else if(Long.class.isAssignableFrom(toClass)) {
			return Optional.of(new Long(fromStringValue[0]));
		} else if(String[].class.isAssignableFrom(toClass)) {
			return Optional.of(fromStringValue);
		} else if(int[].class.isAssignableFrom(toClass)) {
			int[] retValue = new int[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = Integer.parseInt(fromStringValue[i]);
			return Optional.of(retValue);
		} else if(long[].class.isAssignableFrom(toClass)) {
			long[] retValue = new long[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = Long.parseLong(fromStringValue[i]);
			return Optional.of(retValue);
		} else if(Integer[].class.isAssignableFrom(toClass)) {
			Integer[] retValue = new Integer[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new Integer(fromStringValue[i]);
			return Optional.of(retValue);
		} else if(BigDecimal[].class.isAssignableFrom(toClass)) {
			BigDecimal[] retValue = new BigDecimal[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new BigDecimal(fromStringValue[i]);
			return Optional.of(retValue);
		} else if(Long[].class.isAssignableFrom(toClass)) {
			Long[] retValue = new Long[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new Long(fromStringValue[i]);
			return Optional.of(retValue);
		} else {
			return null;
		}
	}

}
