package fd.ng.db.entity;

import fd.ng.core.bean.FeedBean;
import fd.ng.db.jdbc.DatabaseWrapper;

import java.util.*;

public abstract class TableEntity extends FeedBean {
	private static final long serialVersionUID = 4738450537215885587L;
	private transient Set<String> __NullValueFieldNames = new HashSet<>(4);//用实体做update时，存储所有需要设置为null的字段名字

	public void addNullValueFields(String[] fieldNames)
	{
		if(fieldNames==null) return;
		Collections.addAll(__NullValueFieldNames, fieldNames);
	}

	public void addNullValueField(String fieldName) {
		if(fieldName==null) return;
		__NullValueFieldNames.add(fieldName);
	}

	public boolean hasNullValueField(String name) { return __NullValueFieldNames.contains(name); }

	public Set<String> nullValueFields()
	{
		return __NullValueFieldNames;
	}

	/**
	 * 如果一个实体对象被重复使用，那么每次重用前，务必调用被函数，清理上次使用中的状态类数据，比如被设置为null的字段
	 */
	public void clearStatus() {
		__NullValueFieldNames.clear();
	}

//	public static <T extends TableEntity> List<T> getAllData(final DatabaseWrapper db, final Class<T> entityClass) {
//		return EntityOperator.getAllData(db, entityClass);
//	}
//	public static <T extends TableEntity> List<T> gets(final DatabaseWrapper db, final Class<T> entityClass, final Map<String, Object> conds) {
//		return EntityOperator.gets(db, entityClass, conds);
//	}

//	public int add() {
//		return EntityOperator.add(this);
//	}
	public int add(final DatabaseWrapper db) {
		return EntityOperator.add(db, this);
	}
//	public int update() {
//		return EntityOperator.update(this);
//	}
	public int update(final DatabaseWrapper db) {
		return EntityOperator.update(db, this);
	}
//	public int delete() {
//		return EntityOperator.delete(this);
//	}
	public int delete(final DatabaseWrapper db) {
		return EntityOperator.delete(db, this);
	}
//	public int clearAllData() {
//		return EntityOperator.clearAllData(this.getClass());
//	}
//	public int clearAllData(final DatabaseWrapper db) {
//		return EntityOperator.clearAllData(db, this.getClass());
//	}
}
