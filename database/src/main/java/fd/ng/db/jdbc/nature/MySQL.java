package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

import java.util.Set;

public class MySQL extends AbstractNatureDatabase {
	private MySQL() {}
	//mysql建表语句的转义符
	public static final String MYSQL_ESCAPES = "`";

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		if(orginBegin>1) {
			pagedSqlInfo.setSql(sql + " limit ?, ?");
			pagedSqlInfo.setPageNo1(orginBegin - 1);
			pagedSqlInfo.setPageNo2(orginEnd - orginBegin + 1);
		} else {
			pagedSqlInfo.setSql(sql + " limit ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist); // 该参数已经无意义
		}
		return pagedSqlInfo;
	}
	public static String toKeyLabelSql(String tableName, Set<String> columnName) {
		StringBuilder columnSB = new StringBuilder();
		for (String s : columnName) {
			columnSB.append(MYSQL_ESCAPES).append(s).append(MYSQL_ESCAPES).append(COLUMN_SEPARATOR);
		}
		String column = columnSB.toString().substring(0, columnSB.toString().length() - 1);
		return "select " + column + " from " + MYSQL_ESCAPES + tableName + MYSQL_ESCAPES;
	}
}
