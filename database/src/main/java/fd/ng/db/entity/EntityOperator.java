package fd.ng.db.entity;

import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.ClassUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.entity.anno.Table;
import fd.ng.db.jdbc.DatabaseWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class EntityOperator {
	private static final Logger logger = LogManager.getLogger(EntityOperator.class.getName());
	private static final Map<String, Object> GET_ALLOFDATA = new HashMap<>(0); // 如果需要无条件查询整表数据，则使用这个变量

	public static <T extends TableEntity> Optional<T> getEntity(
			final DatabaseWrapper db, final Class<T> entityClass, final Map<String, Object> conds) {
		List<T> list = gets(db, entityClass, conds);
		if(list==null||list.size()<1)
			return Optional.empty();
		else if(list.size()>1) {
			logger.error("Too many Entity({}) for getEntity(), conditions : {}",
					entityClass.getSimpleName(), Arrays.toString(conds.values().toArray()));
			return Optional.empty();
		}
		else
			return Optional.of(list.get(0));
	}

	/**
	 * 获取整表数据。慎用！
	 * @param db DatabaseWrapper DB对象。WEB应用可通过 HttpDataHolder 获得，APP应用可通过DBOperator.attainDB()获得
	 * @param entityClass Class 实体class
	 * @param <T> TableEntity子类
	 * @return 实体List
	 */
	public static <T extends TableEntity> List<T> getAllData(final DatabaseWrapper db, final Class<T> entityClass) {
		return gets(db, entityClass, GET_ALLOFDATA);
	}

	/**
	 * 根据 conds Map 中设置的值，查询对于 entityClass 对应的表数据。
	 * 如果需要查询全表数据，conds仅需要添加一个值： new HashMap(EntityOperator.GET_ALLOFDATA, true);
	 *
	 * @param db DatabaseWrapper DB对象。WEB应用可通过 HttpDataHolder 获得，APP应用可通过DBOperator.attainDB()获得
	 * @param entityClass Class 实体class
	 * @param conds Map 查询条件，key为字段名, value为值。被拼接到 where 后面
	 * @return 实体List
	 */
	public static <T extends TableEntity> List<T> gets(final DatabaseWrapper db, final Class<T> entityClass, final Map<String, Object> conds) {
		Validator.notNull(db, "db must not null!");
		Validator.notNull(entityClass, "entity class must not null!");

		String tableName = entityClass.getSimpleName();
		Table tableAnno = entityClass.getAnnotation(Table.class);
		if(tableAnno!=null&& StringUtil.isNotEmpty(tableAnno.tableName())) {
			tableName = tableAnno.tableName();
		}
		String sqlActual = "select * from " + tableName;
		List<Object> valueList = new ArrayList<>(16); // 存储各个条件的值
		if (conds!=GET_ALLOFDATA) {
			Validator.notEmpty(conds, "condition must not null!");
			final StringBuilder sql = new StringBuilder(128);
			StringBuilder qmark = new StringBuilder(32); // SQL里的问号占位符
			sql.append("select * from ").append(tableName).append(" where ");
			Set<Map.Entry<String, Object>> condsEntries = conds.entrySet();
			for(Map.Entry<String, Object> entry : condsEntries) {
				sql.append(entry.getKey()).append("=? and ");
				valueList.add(entry.getValue());
			}
			if (sql.lastIndexOf("=? and ") > -1)
				sql.delete(sql.length() - 5, sql.length());
			sqlActual = sql.toString();
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("get all data of table : " + tableName);
			}
		}
		return db.query(sqlActual,
				(rs)-> EntityHelper.Rs2EntityCopier.toBeanList(rs, entityClass), valueList);
	}

//	public static int add(final TableEntity entity) { return add(Dbo.attainDB(), entity); }
	public static int add(final DatabaseWrapper db, final TableEntity entity) {
		Validator.notNull(db, "db must not null!");
		Validator.notNull(entity, "entity must not null!");
		Class<?> entityClass = entity.getClass();
		String tableName = entityClass.getSimpleName();
		Table tableAnno = entityClass.getAnnotation(Table.class);
		if(tableAnno!=null&& StringUtil.isNotEmpty(tableAnno.tableName())) {
			tableName = tableAnno.tableName();
		}
		StringBuilder sql=new StringBuilder(128);
		StringBuilder qmark=new StringBuilder(32); // SQL里的问号占位符
		List<Object> valueList = new ArrayList<>(16); // 实际的值
		sql.append("insert into ").append(tableName).append("(");
		try {
			PropertyDescriptor[] pdArr = ClassUtil.propertyDescriptors(entity);
			for (PropertyDescriptor pd : pdArr) {
				if(Class.class.isAssignableFrom(pd.getPropertyType())) continue;
				final Method readMethod = pd.getReadMethod();
				if (pd.getWriteMethod() != null && readMethod != null) {
					final String fieldName = pd.getName();
					final Object value = readMethod.invoke(entity);
					if (value == null) {
						if(!entity.hasNullValueField(fieldName))
							continue; // 没有被明确设置为 null ，跳过
					}
					String FieldToColumn_Name = EntityHelper.getFieldColumnMap(entityClass).getOrDefault(fieldName, fieldName);
					sql.append(FieldToColumn_Name).append(",");
					qmark.append("?,");
					valueList.add(value);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RawlayerRuntimeException(e);
		}
		sql.deleteCharAt(sql.length()-1).append(") values(");
		qmark.deleteCharAt(qmark.length()-1);
		sql.append(qmark).append(")");

		db.beginTrans();
		return db.execute(sql.toString(), valueList);
	}

//	public static int update(final TableEntity entity) { return update(Dbo.attainDB(), entity); }
	public static int update(final DatabaseWrapper db, final TableEntity entity) {
		Validator.notNull(db, "db must not null!");
		Validator.notNull(entity, "entity must not null!");
		Class<?> entityClass = entity.getClass();
		String tableName = entityClass.getSimpleName();
		Table tableAnno = entityClass.getAnnotation(Table.class);
		if(tableAnno!=null&& StringUtil.isNotEmpty(tableAnno.tableName())) {
			tableName = tableAnno.tableName();
		}
		StringBuilder sql=new StringBuilder(128);
		List<Object> valueList = new ArrayList<>(16); // 实际的值
		List<Object[]> primarykeys = new ArrayList<>(1); // 存所有主键
		sql.append("update ").append(tableName).append(" set ");
		try {
			Method method = entityClass.getMethod("isPrimaryKey", String.class);
			PropertyDescriptor[] pdArr = ClassUtil.propertyDescriptors(entity);
			for (PropertyDescriptor pd : pdArr) {
				if(Class.class.isAssignableFrom(pd.getPropertyType())) continue;
				final Method readMethod = pd.getReadMethod();
				if (pd.getWriteMethod() != null && readMethod != null) {
					final String propName = pd.getName(); // 得到属性名
					final Object value = readMethod.invoke(entity); // 得到属性值
					if((boolean)method.invoke(null, propName)) {
						if(value==null) throw new RawlayerRuntimeException("primary key [" + propName + "] must not null!");
						Object[] pk = new Object[]{propName, value}; // 第一个是主键名，第二个是主键值
						primarykeys.add(pk);
						continue;
					}
					if (value == null) {
						if(!entity.hasNullValueField(propName))
							continue;
					}
					// 有注解则用注解，否则就用属性名
					String FieldToColumn_Name = EntityHelper.getFieldColumnMap(entityClass).getOrDefault(propName, propName);
					sql.append(FieldToColumn_Name).append("=?, ");
					valueList.add(value);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RawlayerRuntimeException(e);
		}
		if(primarykeys.isEmpty()) {// update必须设置主键，否则无法更新
			Set<String> pkNames = Collections.emptySet();
			try {
				Method method = entityClass.getMethod("getPrimaryKeyNames");
				pkNames = (Set<String>)method.invoke(null);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				logger.error(e);
			}
			throw new RawlayerRuntimeException("table : [" + tableName + "] must set primary key." +
					" pk=" + pkNames.stream().collect(Collectors.joining(", ")) +
					" entity=" + entity.toString());
		}
		/** 把所有需要设置为null的字段拼接进来 */
//		entity.getNullValueFields().forEach(v->sql.append(v).append("=null, "));
		sql.deleteCharAt(sql.length()-2).append(" where ");
		// 拼条件
		for(Object[] v : primarykeys) {
			sql.append((String)v[0]).append("=? and ");
			valueList.add(v[1]);
		};
		sql.delete(sql.length()-5, sql.length());
//		if(sql.lastIndexOf("=? and ")>-1)//如果不是这样结尾的，说明objOld对象的属性都是空，这样不允许。
//			sql.delete(sql.length()-5, sql.length());
//		else
//			throw new RawlayerRuntimeException("entity : [" + tableName + "] all properties is null!");

		db.beginTrans();
		return db.execute(sql.toString(), valueList);
	}

//	public static int delete(final TableEntity entity) { return delete(Dbo.attainDB(), entity); }
	public static int delete(final DatabaseWrapper db, final TableEntity entity) {
		Validator.notNull(db, "db must not null!");
		Validator.notNull(entity, "entity must not null!");
		Class<?> entityClass = entity.getClass();
		String tableName = entityClass.getSimpleName();
		Table tableAnno = entityClass.getAnnotation(Table.class);
		if(tableAnno!=null&& StringUtil.isNotEmpty(tableAnno.tableName())) {
			tableName = tableAnno.tableName();
		}
		StringBuilder sql=new StringBuilder(128);
		List<Object> valueList = new ArrayList<>(16); // 实际的值
		sql.append("delete from ").append(tableName).append(" where ");
		try {
			PropertyDescriptor[] pdArr = ClassUtil.propertyDescriptors(entity);
			for (PropertyDescriptor pd : pdArr) {
				if(Class.class.isAssignableFrom(pd.getPropertyType())) continue;
				final Method readMethod = pd.getReadMethod();
				if (pd.getWriteMethod() != null && readMethod != null) {
					Object value = readMethod.invoke(entity);
					if (value == null) continue; // TODO 改成判断是否设置成 null 值了
					String propName = pd.getName();
					// 有注解则用注解，否则就用属性名
					String FieldToColumn_Name = EntityHelper.getFieldColumnMap(entityClass).getOrDefault(propName, propName);

					sql.append(FieldToColumn_Name).append("=? and ");
					valueList.add(value);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RawlayerRuntimeException(e);
		}
		if(sql.lastIndexOf("=? and ")>-1)
			sql.delete(sql.length()-5, sql.length());
		else //没有任何条件，则为删除整个表
			//sql=new StringBuilder("delete from ").append(entityName);
			throw new RawlayerRuntimeException("No delete conditions are set for [" + tableName + "], please using clearAllData()!");

		db.beginTrans();
		return db.execute(sql.toString(), valueList);
	}

//	public static int clearAllData(final Class<? extends TableEntity> clz) { return clearAllData(Dbo.attainDB(), clz); }
	/**
	 * 删除全部表数据。
	 * @param clz TableEntity的class
	 * @return 被删除的记录数
	 */
	public static int clearAllData(final DatabaseWrapper db, final Class<? extends TableEntity> clz) {
		Validator.notNull(db, "db must not null!");
		Validator.notNull(clz, "entity class must not null!");
		String tableName = clz.getSimpleName();
		Table tableAnno = clz.getAnnotation(Table.class);
		if(tableAnno!=null&& StringUtil.isNotEmpty(tableAnno.tableName())) {
			tableName = tableAnno.tableName();
		}
		if(logger.isDebugEnabled()) {
			logger.debug("entity clean all data");
		}
		db.beginTrans();
		return db.ExecDDL("truncate table " + tableName);
	}

}
