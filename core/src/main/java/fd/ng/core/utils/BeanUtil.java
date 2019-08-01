package fd.ng.core.utils;

import fd.ng.core.cache.MuchReadFewWriteCache;
import fd.ng.core.cache.OftenCache;
import fd.ng.core.exception.internal.RuntimeOnlyMessageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanUtil {
	private static final Logger logger = LogManager.getLogger(BeanUtil.class.getName());
	public BeanUtil() { throw new AssertionError("No BeanUtil instances for you!"); }

	/** Bean Field 缓存，包括各级父类 */
	private static final MuchReadFewWriteCache<Class<?>, Map<String, Field>> FIELDS_CACHE = new MuchReadFewWriteCache<>();
	/** Bean Field 缓存，不包括各级父类 */
	private static final MuchReadFewWriteCache<Class<?>, Map<String, Field>> DECLARED_FIELDS_CACHE = new MuchReadFewWriteCache<>();

	/**
	 * 因为没有 ConcurrentHashSet，所以，用这种方式，
	 * 等同于构造了一个 Concurrent 能力的 Set。
	 */
	private static final Set<Class<?>> somthingCache =
			Collections.newSetFromMap(new ConcurrentHashMap<>(64));

	/**
	 * 根据名字获取类中的Field，也包括父类的字段， 字段不存在则返回<code>null</code>
	 * 有缓存，高效率
	 * 该 Field 可通过反射直接读写
	 *
	 * @param beanClass 被查找字段的类,不能为null
	 * @param name 字段名
	 * @return 字段
	 */
	public static Field getField(Class<?> beanClass, String name) {
		Map<String, Field> fieldMap = getAllVisibleFields(beanClass);
		if(fieldMap==null) {
			logger.warn("get Null class({}) from Cache!", beanClass);
			return null;
		}
		return fieldMap.get(name);
	}

	/**
	 * 获取指定类的所有Field，也包括父类的字段，字段不存在则返回<code>null</code>
	 * 有缓存，高效率
	 * 该 Field 可通过反射直接读写
	 *
	 * @param beanClass 被查找字段的类,不能为null
	 * @return Map(Field名, Field)
	 */
	public static Map<String, Field> getAllVisibleFields(Class<?> beanClass) {
		final Map<String, Field> fieldMap0 = FIELDS_CACHE.get(beanClass);
		if(fieldMap0!=null) return fieldMap0;
		// 不存在，则获取BEAN的信息并缓存后返回
		List<Field> fields = ClassUtil.getAllVisibleFields(beanClass, true);
		final Map<String, Field> fieldMap = new HashMap<>(fields.size());
		fields.forEach(field -> {
//			if(!(Modifier.isFinal(field.getModifiers())||Modifier.isStatic(field.getModifiers())))
				fieldMap.put(field.getName(), field);
		});
		FIELDS_CACHE.putIfAbsent(beanClass, fieldMap);
		return fieldMap;
	}

	/**
	 * 获取所有field（不包括父类）
	 * 与JDK getDeclaredFields 的区别是：有缓存
	 * 该 Field 可通过反射直接读写
	 *
	 * @param beanClass 被查找字段的类,不能为null
	 * @return Map(Field名, Field)
	 */
	public static Map<String, Field> getDeclaredFields(final Class<?> beanClass) {
		final Map<String, Field> fieldMap0 = DECLARED_FIELDS_CACHE.get(beanClass);
		if(fieldMap0!=null) return fieldMap0;
		// 不存在，则获取BEAN的信息并缓存后返回
		final Field[] fields = beanClass.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		final Map<String, Field> fieldMap = new HashMap<>(fields.length);
		for (final Field field : fields) {
//			if(!(Modifier.isFinal(field.getModifiers())||Modifier.isStatic(field.getModifiers())))
				fieldMap.put(field.getName(), field);
		}
		DECLARED_FIELDS_CACHE.putIfAbsent(beanClass, fieldMap);
		return fieldMap;
	}

	/**
	 * 把字符串数组中的值，造型成给定类型的对象。
	 * 目前只支持主类型和主类型数组。
	 * 不支持float/double，因为可以用BigDecimal代替。
	 *
	 * @param fromStringValue String[] 需要做造型转换的原值。
	 * @param toClass Class<?> 造型成哪种类型。
	 * @param //unsupportedTypeToNull boolean 对于不支持的class类型，返回null还是抛出异常。
	 * @return Object 造型后的对象。如果碰到不支持的类型，则打印日志并返回 null 。
	 */
	public static Object castStringToClass(String[] fromStringValue, Class<?> toClass) throws ArgumentNullvalueException, ArgumentUnsupportedTypeException {
		Validator.notNull(toClass);
		if(fromStringValue==null||fromStringValue.length==0) {
			if(int.class.isAssignableFrom(toClass) || long.class.isAssignableFrom(toClass)
			|| int[].class.isAssignableFrom(toClass) || long[].class.isAssignableFrom(toClass)) { // 对于主类型，不能生成null返回。
				throw new ArgumentNullvalueException();
			}
			else
				return null;
		}

		if (String.class.isAssignableFrom(toClass)) {
			return fromStringValue[0];
		} else if(int.class.isAssignableFrom(toClass)) {
			return Integer.parseInt(fromStringValue[0]);
		} else if(Integer.class.isAssignableFrom(toClass)) {
			return new Integer(fromStringValue[0]);
		} else if(BigDecimal.class.isAssignableFrom(toClass)) {
			return new BigDecimal(fromStringValue[0]);
		} else if(long.class.isAssignableFrom(toClass)) {
			return Long.parseLong(fromStringValue[0]);
		} else if(Long.class.isAssignableFrom(toClass)) {
			return new Long(fromStringValue[0]);
		} else if(String[].class.isAssignableFrom(toClass)) {
			return fromStringValue;
		} else if(int[].class.isAssignableFrom(toClass)) {
			int[] retValue = new int[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = Integer.parseInt(fromStringValue[i]);
			return retValue;
		} else if(long[].class.isAssignableFrom(toClass)) {
			long[] retValue = new long[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = Long.parseLong(fromStringValue[i]);
			return retValue;
		} else if(Integer[].class.isAssignableFrom(toClass)) {
			Integer[] retValue = new Integer[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new Integer(fromStringValue[i]);
			return retValue;
		} else if(BigDecimal[].class.isAssignableFrom(toClass)) {
			BigDecimal[] retValue = new BigDecimal[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new BigDecimal(fromStringValue[i]);
			return retValue;
		} else if(Long[].class.isAssignableFrom(toClass)) {
			Long[] retValue = new Long[fromStringValue.length];
			for(int i=0; i<fromStringValue.length; i++) retValue[i] = new Long(fromStringValue[i]);
			return retValue;
		} else {
//			if(unsupportedTypeToNull){
//				logger.info(String.format("Unsupported Class type : [%s]", toClass.getSimpleName()));
//				return null;
//			}
//			else
				throw new ArgumentUnsupportedTypeException();
		}
	}

	public static boolean isNumberClass(Object obj) {
		if(obj==null) return false;
		else if( obj instanceof Number )
			return true;
		else if ( obj.getClass().isAssignableFrom(int.class) || obj.getClass().isAssignableFrom(long.class)
					|| obj.getClass().isAssignableFrom(float.class) || obj.getClass().isAssignableFrom(double.class) )
			return true;
		else
			return false;
	}

	public static class ArgumentNullvalueException extends RuntimeOnlyMessageException {
		private static final long serialVersionUID = 5452559469812766233L;
		public ArgumentNullvalueException() {}
	}
	public static class ArgumentUnsupportedTypeException extends RuntimeOnlyMessageException{
		private static final long serialVersionUID = -5349355360052031687L;
		public ArgumentUnsupportedTypeException() {}
	}
}
