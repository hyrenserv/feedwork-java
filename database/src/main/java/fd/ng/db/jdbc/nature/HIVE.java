package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

import java.util.Set;

/**
 * @program: feedwork
 * @description:
 * @author: xchao
 * @create: 2020-05-21 10:51
 */
public class HIVE  extends AbstractNatureDatabase{
	private HIVE() {}

	public static final String HIVE_ESCAPES = "`";
	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin != 1) throw new FrameworkRuntimeException("Hive only supports retrieving pre-orginend data");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		pagedSqlInfo.setSql(sql + " limit ?");
		pagedSqlInfo.setPageNo1(orginEnd);
		pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist);

		return pagedSqlInfo;
	}
	public static String toKeyLabelSql(String tableName, Set<String> columnName) {
		StringBuilder columnSB = new StringBuilder();
		for (String s : columnName) {
			columnSB.append(HIVE_ESCAPES).append(s).append(HIVE_ESCAPES).append(COLUMN_SEPARATOR);
		}
		String column = columnSB.toString().substring(0, columnSB.toString().length() - 1);
		return "select " + column + " from " + tableName;
	}
}
