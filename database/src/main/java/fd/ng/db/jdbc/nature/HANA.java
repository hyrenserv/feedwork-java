package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

import java.util.Set;

/**
 * 这SB难道又是抄袭的PGSQL？
 */
public class HANA extends AbstractNatureDatabase {
	private HANA() {}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		if(orginBegin>1) {
			pagedSqlInfo.setSql(sql + " limit ? offset ?");
			pagedSqlInfo.setPageNo1(orginEnd - orginBegin + 1);
			pagedSqlInfo.setPageNo2(orginBegin - 1); // 因为 pg 是从0开始计算记录位置
		} else {
			pagedSqlInfo.setSql(sql + " limit ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist);
		}
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
