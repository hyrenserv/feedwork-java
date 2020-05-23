package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

import java.util.Set;

public class Oracle9iAbove extends AbstractNatureDatabase {
	private Oracle9iAbove(){}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		sql = sql.trim();
		boolean hasForUpdate = false;
		if (sql.toLowerCase().endsWith(ForUpdate)) {
			sql = sql.substring( 0, sql.length() - 11 );
			hasForUpdate = true;
		}

		final StringBuilder _pagedSql = new StringBuilder( sql.length() + 100 );
		if(orginBegin>1) {
			_pagedSql.append("select * from ( select row_.*, rownum rownum_ from ( ");
		} else {
			_pagedSql.append("select * from ( ");
		}
		_pagedSql.append(sql);
		if(orginBegin>1) {
			_pagedSql.append(" ) row_ where rownum <= ?) where rownum_ >= ?");//因为出入的值是从1开，这里需要包含当前数据
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(orginBegin);
		}
		else {
			_pagedSql.append(" ) where rownum <= ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist); // 该参数已经无意义
		}
		if (hasForUpdate) {
			_pagedSql.append(ForUpdate);
		}
		pagedSqlInfo.setSql(_pagedSql.toString());
		return pagedSqlInfo;
	}

	public static String toKeyLabelSql(String tableName, Set<String> columnName) {
		StringBuilder columnSB = new StringBuilder();
		for (String s : columnName) {
			columnSB.append(s).append(COLUMN_SEPARATOR);
		}
		String column = columnSB.toString().substring(0, columnSB.toString().length() - 1);
		return "select " + column + " from " + tableName;
	}
}
