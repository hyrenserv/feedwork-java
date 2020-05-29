package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class Oracle9iAbove extends AbstractNatureDatabase {
	private Oracle9iAbove(){}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if (orginBegin < 1) throw new FrameworkRuntimeException("page begin must greater than 0");
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

	public static String toKeyLabelSql(String tableName, Set<String> columnName,String databaseName) {
		StringBuilder columnSB = new StringBuilder();
		for (String s : columnName) {
			columnSB.append(s).append(COLUMN_SEPARATOR);
		}
		String column = columnSB.toString().substring(0, columnSB.toString().length() - 1);
		if(!StringUtil.isEmpty(databaseName)){
			tableName = databaseName+"."+tableName;
		}
		return "select " + column + " from " + tableName;
	}

	public static String getDatabase(final DatabaseWrapper db,DatabaseMetaData dbMeta){
		return db.getDatabaseName().toUpperCase();
		/*ResultSet rsTables = null;
		if (!StringUtil.isBlank(db.getetTablespaceName())) {
			String sql = "select concat(concat(TABLESPACE_NAME,'.'),TABLE_NAME) table_name,'' TABLE_TYPE,'' TABLE_CAT,'' TABLE_SCHEM,''REMARKS " +
					"from all_tables where TABLESPACE_NAME= '" + db.getetTablespaceName().toUpperCase() + "'";
			if (!StringUtil.isBlank(tableNamePattern)) {
				sql = sql + " and table_name = '" + tableNamePattern + "'";
			}
			rsTables = db.queryGetResultSet(sql);

		} else {
			DatabaseMetaData dbMeta = db.getConnection().getMetaData();
			if (!StringUtil.isBlank(tableNamePattern)) {
				rsTables = dbMeta.getTables(null, "%", tableNamePattern, type);
			} else {
				rsTables = dbMeta.getTables(null, db.getetTablespaceName(), "%", type);
			}
		}
		return rsTables;*/
	}
}
