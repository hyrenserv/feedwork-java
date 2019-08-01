package fd.ng.db.entity;

import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.utils.ArrayUtil;
import fd.ng.core.utils.BeanUtil;
import fd.ng.core.utils.ClassUtil;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.resultset.helper.ResultSetToBeanHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityHelper {
	private static final Logger logger = LogManager.getLogger(EntityHelper.class.getName());
	public static final ResultSetToBeanHelper Rs2EntityCopier = new ResultSetToBeanHelper();
	/**
	 * 实体类 => (属性名 => 列名(Column注解))
	 */
	private static Map<Class<?>, Map<String, String>> entityClassFieldColumnMap = new HashMap<>();

	static {
		try {
			List<Class<?>> entityClassList = ClassUtil.getClassListBySuper(AppinfoConf.AppBasePackage, TableEntity.class);
			if(entityClassList.size()<3) {
				logger.warn("Only {} entities in this project! ", entityClassList.size());
			}
			entityClassList.forEach(EntityHelper::initEntityFieldColumn);
			entityClassFieldColumnMap = Collections.unmodifiableMap(entityClassFieldColumnMap);
		} catch (Exception e) {
			throw new Error("init Entity Column info failed!", e);
		}
	}

	/**
	 * 得到一个类的（属性->Column注解）的关系Map
	 * @param entityClass 实体类
	 * @return （属性->Column注解）的关系Map
	 */
	public static Map<String, String> getFieldColumnMap(Class<?> entityClass) {
		return entityClassFieldColumnMap.getOrDefault(entityClass, Collections.emptyMap());
	}

	private static void initEntityFieldColumn(Class<?> entityClass) {
		// 获取并遍历该实体类中所有的字段（不包括父类中的方法。因为实体是每张表一个，不存在继承）
		Map<String, Field> fieldMap = BeanUtil.getDeclaredFields(entityClass);
		if (fieldMap.size()>0) {
			// 存放定义了Column注解的 字段名 -> 列名 的映射关系
			Map<String, String> fieldColumnMap = new HashMap<>();
			for (final Map.Entry<String, Field> m : fieldMap.entrySet()) {
				final Field field = m.getValue();
				Column fieldColumnAnno = field.getAnnotation(Column.class);
				// TODO 如果要做字段下划线向驼峰字段的转化，应该在这里处理。下面这个空则continue的逻辑要删除。因为现在只存了有注解的字段
				if(fieldColumnAnno==null) continue;
				String columnName = fieldColumnAnno.value();
				fieldColumnMap.put(field.getName(), columnName);
			}
			entityClassFieldColumnMap.put(entityClass, Collections.unmodifiableMap(fieldColumnMap));
		}
	}
}
