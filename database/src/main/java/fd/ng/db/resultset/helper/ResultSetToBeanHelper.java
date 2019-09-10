package fd.ng.db.resultset.helper;

import fd.ng.core.cache.MuchReadFewWriteCache;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.BeanUtil;
import fd.ng.core.utils.ClassUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.entity.EntityHelper;
import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.resultset.TooManyRecordsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * 对 Bean 的通用处理封装。
 */
public class ResultSetToBeanHelper {
	private static final Logger logger = LogManager.getLogger(ResultSetToBeanHelper.class.getName());
	private static final int PROPERTY_NOT_FOUND = -1;

	/**
	 * 缓存已经处理过的类
	 * 实体类 => (属性名 => 列名(Column注解))
	 */
	private static final MuchReadFewWriteCache<Class<?>, Map<String, String>> cachedBeanAnnoInfo = new MuchReadFewWriteCache<>();

	/**
	 * 使用与表列名不一样的属性名，在构造函数中传入映射关系。
	 * 如果传入了这个映射关系，那么属性上的注解会被忽略。
	 * 也就是说，属性上定义的列名注解，其优先级低于动态传入的映射关系。
	 */
	private final Map<String, String> columnToPropertyOverrides;
	/**
	 * 如果SQL返回对应的值对象是主类型，那么使用这个默认值代替
	 */
	private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();

	static {
		primitiveDefaults.put(Integer.TYPE, Integer.MIN_VALUE);
		primitiveDefaults.put(Short.TYPE, (short)Integer.MIN_VALUE);
		primitiveDefaults.put(Byte.TYPE, (byte)Integer.MIN_VALUE);
		primitiveDefaults.put(Float.TYPE, Float.MIN_VALUE);
		primitiveDefaults.put(Double.TYPE, Double.MIN_VALUE);
		primitiveDefaults.put(Long.TYPE, Long.MIN_VALUE);
		primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
		primitiveDefaults.put(Character.TYPE, (char)0);
	}

	public ResultSetToBeanHelper() {
		this(Collections.emptyMap());
	}
	public ResultSetToBeanHelper(final Map<String, String> columnToPropertyOverrides) {
		super();
		this.columnToPropertyOverrides = columnToPropertyOverrides;
	}

	public <T> T toBean(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		if (rs==null||!rs.next()) return null;
		try {
			PropertyDescriptor[] props = ClassUtil.propertyDescriptors(classOfBean);
			ResultSetMetaData rsmd = rs.getMetaData();
			int[] columnToProperty = this.mapColumnsToProperties(classOfBean, rsmd, props);
			T bean = this.populateBean(rs, classOfBean, props, columnToProperty);
			if(rs.next()) // 查询到多余一条数据，不能使用本方法
				throw new TooManyRecordsException(classOfBean.getName());
			else
				return bean;
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
			throw new SQLException(e);
		}
	}

	public <T> List<T> toBeanList(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		if (rs==null||!rs.next()) {
			return Collections.emptyList();
		}

		PropertyDescriptor[] props = ClassUtil.propertyDescriptors(classOfBean);
		ResultSetMetaData rsmd = rs.getMetaData();
		int[] columnToProperty = this.mapColumnsToProperties(classOfBean, rsmd, props);

		List<T> results = new ArrayList<>();
		try {
			do {
				T bean = this.populateBean(rs, classOfBean, props, columnToProperty);
				results.add(bean);
			} while (rs.next());
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new SQLException(e);
		}
		return results;
	}

	private <T> T populateBean(ResultSet rs, Class<? extends T> classOfBean, PropertyDescriptor[] props, int[] columnToProperty)
			throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
		T bean = classOfBean.newInstance();
		for (int i = 1; i < columnToProperty.length; i++) {
			if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
				continue; // 表示当前列，不在bean属性中。即：bean里面没有定义与当前列名匹配的属性
			}
			// 得到当前列对应的bean属性的类型
			PropertyDescriptor prop = props[columnToProperty[i]];
			Class<?> propType = prop.getPropertyType();

			Object value = null;
			if (propType != null) {
				value = this.processColumn(rs, i, propType);

				if (value == null && propType.isPrimitive()) {
					value = primitiveDefaults.get(propType);
				}
			}
			/*
			FIXME
			propType : 是 Bean 中当前字段的 Java 类型
			value    : 是表中相应字段的数据
			如果彼此不一致，会导致异常。
			比如：DB表中字段是 Decimal(10)，Bean中相应字段是 Long，就会导致下面的反射赋值时出现异常
			解决办法：
			在循环外面通过rs.getMetaData()提前获取每个字段的数据类型，
			对于Decimal(10)的字段，记录位置，并且在本循环中强制类型转换为Long
			 */
			this.callSetter(bean, prop, value);
		}
		return bean;
	}

	private Object processColumn(ResultSet rs, int index, Class<?> propType)
			throws SQLException {

		Object retval = rs.getObject(index);
		if ( !propType.isPrimitive() && retval == null ) {
			return null;
		}

		// TODO 这里可以增加对列值的转换处理函数。通过构造函数注入进来即可
		return retval;

	}

	private void callSetter(Object target, PropertyDescriptor prop, Object value)
			throws InvocationTargetException, IllegalAccessException {
		Method setter = prop.getWriteMethod();
		if (setter == null || setter.getParameterTypes().length != 1) {
			logger.warn("bean [{}], PropertyDescriptor [{}] getWriteMethod fail!", target, prop);
			return;
		}
		setter.invoke(target, value);
	}

	// 得到一个属性在返回的结果集中的位置（第几个）
	private int[] mapColumnsToProperties(final Class<?> classOfBean, final ResultSetMetaData rsmd, final PropertyDescriptor[] props) throws SQLException {

		int cols = rsmd.getColumnCount();
		int[] columnToProperty = new int[cols + 1];
		Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

		// key: 实体类中设置了 Column 注解的属性字段名。 value: Column 注解里定义的列名
		Map<String, String> annoColumnNameOfEntityField = null;
		if(TableEntity.class.isAssignableFrom(classOfBean))
			annoColumnNameOfEntityField = EntityHelper.getFieldColumnMap(classOfBean);

		for (int col = 1; col <= cols; col++) {
			String columnName = rsmd.getColumnLabel(col);
			if (StringUtil.isEmpty(columnName)) { // 没有别名，则使用原始列名
				columnName = rsmd.getColumnName(col);
			}
			// 获取列名对应的属性名
			String propertyName = columnToPropertyOverrides.get(columnName);
			// 1. 当前列名，在构造函数中定义了对应的属性字段名
			// 那么，使用这个定义好的属性名，循环对每个属性字段中找一遍，找到其位置
			if(propertyName!=null) {
				for (int i = 0; i < props.length; i++) {
					PropertyDescriptor pd = props[i];
					if(Class.class.isAssignableFrom(pd.getPropertyType())) continue;
					if (propertyName.equalsIgnoreCase(props[i].getName())) {
						columnToProperty[col] = i;
						break;
					}
				}
				// 虽然对当前列名定义了属性名，但是该属性名在classOfBean中没有找到
				if(columnToProperty[col]==PROPERTY_NOT_FOUND) {
					logger.debug("class({}) can not found property({}) by column({}), will be use other ways.", classOfBean, propertyName, columnName);
				} else {
					continue; // 找到当前列名对应的属性字段，所以跳过下面的处理，继续处理下一个列名
				}
			}
			// 2. 当前列名，在构造函数中没定义属性名，或者虽然定义了，但是找不到匹配的属性，则使用属性注解或属性名进行匹配查找
			for (int i = 0; i < props.length; i++) {
				PropertyDescriptor pd = props[i];
				if(Class.class==pd.getPropertyType()) continue;
				String propName = pd.getName(); // 属性名

				if(annoColumnNameOfEntityField!=null) { // 这是一个实体BEAN，则通过缓存的实体map获取属性上的注解名
					propName = annoColumnNameOfEntityField.getOrDefault(propName, propName);
				} else {
					// 这是一个普通bean，则获取该属性对应的field之上的注解
					Field propField = BeanUtil.getField(classOfBean, propName);
					if(propField!=null) {
						Column columnAnno = propField.getAnnotation(Column.class);
						if(columnAnno!=null) // 属性字段有注解，则使用注解名
							propName = columnAnno.value();
					} else {
						// 这个属性没有对应的field（比如只是一个setXXX，或者属性与字段不同名
						logger.debug("class({}) property({}) not matched field.", classOfBean, pd);
					}
				}
				if (columnName.equalsIgnoreCase(propName)) {
					columnToProperty[col] = i;
					break;
				}
			}
		}

		return columnToProperty;
	}
}
