package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.db.jdbc.DatabaseWrapper;

import java.sql.DatabaseMetaData;
import java.util.Set;

public class DB2V1 extends AbstractNatureDatabase {
	private DB2V1() {}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		if(orginBegin>1) {
			StringBuilder _pagedSql = new StringBuilder( sql.length()+250 );
			_pagedSql.append( "select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ")
					.append( sql )
					.append( " fetch first ?" )//limit
					.append( " rows only ) as inner2_ ) as inner1_ where rownumber_ > ?" )//offset
					.append( " order by rownumber_" );
			pagedSqlInfo.setSql(_pagedSql.toString());
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(orginBegin);
		} else {
			pagedSqlInfo.setSql(sql + " fetch first ? rows only");
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

	public String getDatabase(final DatabaseWrapper db, DatabaseMetaData dbMeta) {
		return "jence_user";
	}
}
