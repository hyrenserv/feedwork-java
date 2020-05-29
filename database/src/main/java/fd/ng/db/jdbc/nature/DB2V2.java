package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.db.jdbc.DatabaseWrapper;

import java.sql.DatabaseMetaData;
import java.util.Set;

public class DB2V2 extends AbstractNatureDatabase {
	private DB2V2() {}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		StringBuilder _pagedSql = new StringBuilder(sql.length() + 100);
		_pagedSql.append("select * from ( select ");
		_pagedSql.append(" row_.* from ( ").append(sql).append(" ) as row_");
		_pagedSql.append(" ) as temp_ where rownumber_ ");
		if( orginBegin>1 ) {
			_pagedSql.append("between ?+1 and ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(orginBegin);
		} else {
			_pagedSql.append("<= ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist);
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

	public String getDatabase(final DatabaseWrapper db, DatabaseMetaData dbMeta) {
		return "jence_user";
	}
}
