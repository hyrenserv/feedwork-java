package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

public abstract class AbstractNatureDatabase {
	static final String ForUpdate = " for update";
	//拼接sql字段的分隔符
	static final String COLUMN_SEPARATOR = ",";
	/**
	 * 根据原始 sql ，生成用于计算查询数据量的 count sql。
	 * 本方法，理论上，适用于所有数据库
	 *
	 * @param sql 原始sql
	 * @return 用于计算原始sql查询数据量的 count sql
	 */
	public static String getCountSqlType1(final String sql) {
		return "SELECT COUNT(1) as COUNTS FROM (" + sql + ") as FDNGTMPTBL";
	}

	/**
	 * 根据原始 sql ，生成用于计算查询数据量的 count sql。
	 * 本方法，适用的数据库包括： Mysql
	 *
	 * @param sql 原始sql
	 * @return 用于计算原始sql查询数据量的 count sql
	 */
	public static String getCountSqlType2(String sql) {
		sql = sql.trim();
		String tmp_sql=sql.toUpperCase();
		if(!tmp_sql.startsWith("SELECT "))
			throw new FrameworkRuntimeException( "not regular sql! sql : " + sql );
		int loc = tmp_sql.indexOf(" FROM ");
		int offsetSkipFrom = 6;
		if (loc==-1) {
			loc = tmp_sql.indexOf(" FROM\n");
			if (loc==-1) {
				loc = tmp_sql.indexOf("\nFROM ");
				if (loc==-1) {
					loc = tmp_sql.indexOf("\nFROM\n");
				}
			}
		}
		if(loc==-1)
			throw new FrameworkRuntimeException("Not regular sql! sql : " + sql );
		String tmpSubSql = sql.substring( loc+offsetSkipFrom );
		return "SELECT COUNT(*) as COUNTS FROM " + tmpSubSql;
	}
}
