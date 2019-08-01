package fd.ng.core.utils.beans.json;

import fd.ng.core.bean.FeedBean;
import fd.ng.db.entity.EntityOperator;
import fd.ng.db.jdbc.DatabaseWrapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbsEntity  {
	private static final long serialVersionUID = -11L;
	private transient Set<String> primaryKeys = new HashSet<>(2);
	private transient Set<String> nullValueFieldNames = new HashSet<>(4);//用实体做update时，存储所有需要设置为null的字段名字
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public boolean isPrimaryKey(String name) { return primaryKeys.contains(name); }
	public Set<String> getPrimaryKeyNames() { return primaryKeys; }
	protected void addPKName(String name) { primaryKeys.add(name); }

	public void addNullValueFields(String[] fieldNames)
	{
		if(fieldNames==null) return;
		Collections.addAll(nullValueFieldNames, fieldNames);
	}

	public void addNullValueField(String fieldName)
	{
		if(fieldName==null) return;
		nullValueFieldNames.add(fieldName);
	}

	public Set<String> getNullValueFields()
	{
		return nullValueFieldNames;
	}

	public int add(final DatabaseWrapper db) {
		return EntityOperator.add(db, null);
	}
	//	public int update() {
//		return EntityOperator.update(this);
//	}
	public int update(final DatabaseWrapper db) {
		return EntityOperator.update(db, null);
	}
	//	public int delete() {
//		return EntityOperator.delete(this);
//	}
	public int delete(final DatabaseWrapper db) {
		return EntityOperator.delete(db, null);
	}
}
