package fd.ng.db.meta;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.DBException;
import fd.ng.db.conf.ConnWay;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.nature.PagedSqlInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaOperator {
	private static final Logger logger = LogManager.getLogger(MetaOperator.class.getName());

	public MetaOperator() {
		throw new AssertionError("No MetaOperator instances for you!");
	}

	/**
	 * 得到当前数据库中，当前连接用户的所有表和视图的基本信息（只包含表的基本信息，不含列信息）
	 *
	 * @param db DatabaseWrapper
	 * @return 所有表的基本信息
	 */
	public static List<TableMeta> getTables(final DatabaseWrapper db) {
		return getTables(db, null);
	}

	/**
	 * 得到当前数据库中，当前连接用户的所有表和视图的基本信息（只包含表的基本信息，不含列信息）
	 *
	 * @param db               DatabaseWrapper
	 * @param tableNamePattern 表名
	 *                         null：获取所有表
	 *                         指定名称：支持通配符：单字符（“_”）,多字符（“%”）
	 * @return 所有表的基本信息
	 */
	public static List<TableMeta> getTables(final DatabaseWrapper db, final String tableNamePattern) {
		try (ResultSet rs = db.getConnection().getMetaData().getTables(
				null, "%", tableNamePattern, new String[]{"TABLE", "VIEW"})) {
			List<TableMeta> result = new ArrayList<>();
			while (rs.next()) {
				TableMeta tblMeta = new TableMeta();
				tblMeta.setTableName(rs.getString("TABLE_NAME"));
				tblMeta.setType(rs.getString("TABLE_TYPE"));
				tblMeta.setDbname(rs.getString("TABLE_CAT"));
				tblMeta.setUsername(rs.getString("TABLE_SCHEM"));
//				tblMeta.setRemarks(rsThis.getString("REMARKS"));
				result.add(tblMeta);
			}
			return result;
		} catch (SQLException e) {
			throw new DBException(db.getID(), "get tables failed", e);
		}
	}

	/**
	 * 得到当前数据库中，当前连接用户，符合通配模式的所有表和视图信息（含列信息）
	 *
	 * @param db DatabaseWrapper
	 * @return 所有表的详细信息（含列）
	 */
	public static List<TableMeta> getTablesWithColumns(final DatabaseWrapper db) {
		return getTablesWithColumns(db, null);
	}

	/**
	 * 得到当前数据库中，当前连接用户，符合通配模式的所有表和视图信息（含列信息）
	 *
	 * @param db               DatabaseWrapper
	 * @param tableNamePattern 表名
	 *                         null：获取所有表
	 *                         指定名称：支持通配符：单字符（“_”）,多字符（“%”）
	 * @return 所有表的详细信息（含列）
	 */
	public static List<TableMeta> getTablesWithColumns(final DatabaseWrapper db, final String tableNamePattern) {
		List<TableMeta> result = new ArrayList<>();
		ResultSet rsTables = null;
		ResultSet rsPK = null;
		ResultSet rsColumnInfo = null;
		try {
			DatabaseMetaData dbMeta = db.getConnection().getMetaData();
			String[] type = {"TABLE", "VIEW"};
			String schemaPattern = "%";// meta.getUserName();// 数据库的用户

			String userName = dbMeta.getUserName();
			if (db.getDbtype() == Dbtype.ORACLE) {
				schemaPattern = userName;
				if (null != schemaPattern) schemaPattern = schemaPattern.toUpperCase();
			} else if (db.getDbtype() == Dbtype.MYSQL) {
				schemaPattern = userName;
			} else if (db.getDbtype() == Dbtype.DB2V1 || db.getDbtype() == Dbtype.DB2V2) {
				schemaPattern = "jence_user";
			}

			if (!StringUtil.isBlank(tableNamePattern)) {
				rsTables = dbMeta.getTables(null, schemaPattern, tableNamePattern, type);
			} else {
				rsTables = dbMeta.getTables(null, schemaPattern, "%", type);
			}
			while (rsTables.next()) {
				TableMeta tblMeta = new TableMeta();
				final String tableName = rsTables.getString("TABLE_NAME");
				tblMeta.setTableName(tableName);
				tblMeta.setType(rsTables.getString("TABLE_TYPE"));
				tblMeta.setDbname(rsTables.getString("TABLE_CAT"));
				tblMeta.setUsername(rsTables.getString("TABLE_SCHEM"));
				tblMeta.setRemarks(rsTables.getString("REMARKS"));

				rsPK = dbMeta.getPrimaryKeys(null, null, tableName);
				while (rsPK.next()) {
					tblMeta.addPrimaryKey(rsPK.getString("COLUMN_NAME"));
					// PK_NAME String => 主键的名称（可为 null）
					// KEY_SEQ short  => 主键中的序列号（值 1 表示主键中的第一列，值 2 表示主键中的第二列）
				}
				rsPK.close();

				rsColumnInfo = dbMeta.getColumns(null, "%", tableName, "%");
				fillColumnMeta(db, tblMeta, rsColumnInfo);
				rsColumnInfo.close();

				result.add(tblMeta);
			}
			return result;
		} catch (SQLException e) {
			throw new DBException(db.getID(), "Get table info failed. tableNamePattern=" + tableNamePattern, e);
		} finally {
			try {
				if (rsPK != null) rsPK.close();
			} catch (SQLException e) {
			}
			try {
				if (rsColumnInfo != null) rsColumnInfo.close();
			} catch (SQLException e) {
			}
			try {
				if (rsTables != null) rsTables.close();
			} catch (SQLException e) {
			}
		}
	}

	private static void fillColumnMeta(final DatabaseWrapper db, final TableMeta tblMeta, final ResultSet rs) throws SQLException {
//		String currentTableName = null;
		while (rs.next()) {
//			String tableName = rs.getString( "TABLE_NAME" );
//			if(tableName==null) {
//				logger.debug("current table name is null!");
//				continue;
//			}
//			else if(!tableName.equals(currentTableName)) {
//				currentTableName = tableName;
//			}
			String tableName = rs.getString("TABLE_NAME");
			ColumnMeta columnMeta = new ColumnMeta();
			String colName = rs.getString("COLUMN_NAME");
			columnMeta.setName(colName);
			int type = rs.getInt("DATA_TYPE");
			columnMeta.setTypeOfSQL(type);
			columnMeta.setTypeName(rs.getString("TYPE_NAME"));
			columnMeta.setLength(rs.getInt("COLUMN_SIZE"));
			columnMeta.setScale(rs.getInt("DECIMAL_DIGITS")); // 精度

			int isNullable = rs.getInt("NULLABLE");//是否为空
			columnMeta.setNullable((ResultSetMetaData.columnNullable == isNullable));
//			String sisNull = rs.getString( "IS_NULLABLE" ); 或者，使用这个，然后对返回值的 yes/no 做比较判断。到底用哪个？？？

			tblMeta.addColumnMeta(columnMeta);
		}
	}

	/**
	 * 得到SQL中的每列信息。
	 *
	 * @param db  DatabaseWrapper
	 * @param sql 比如多表查询的SQL、单表查询但是设置了别名的SQL等等。
	 * @return Map<列名, ColumnMeta>
	 */
	public static Map<String, ColumnMeta> getSqlColumnMeta(final DatabaseWrapper db, final String sql) {
		PreparedStatement pstmtThis = null;
		ResultSet rsThis = null;
		try {
			PagedSqlInfo pagedSqlInfo = db.getDbtype().ofPagedSql(sql, 1, 1);
			pstmtThis = db.getConnection().prepareStatement(pagedSqlInfo.getSql());
			if (pagedSqlInfo.getPageNo1() != PagedSqlInfo.PageNoValue_NotExist) pstmtThis.setInt(1, 1);
			if (pagedSqlInfo.getPageNo2() != PagedSqlInfo.PageNoValue_NotExist) pstmtThis.setInt(2, 1);
			rsThis = pstmtThis.executeQuery();
			ResultSetMetaData rsmd = rsThis.getMetaData();
			int columnCount = rsmd.getColumnCount();
			Map<String, ColumnMeta> columnMetaMap = new HashMap<>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				ColumnMeta columnMeta = new ColumnMeta();
				final String columnName = rsmd.getColumnName(i);        // 列名
				columnMeta.setName(columnName);
				String ColumnClassName = rsmd.getColumnClassName(i);    // 使用getObject取结果时，指明 Java 类的完全限定名称。
				int disSize = rsmd.getColumnDisplaySize(i);             // 列的最大标准宽度，以字符为单位

				columnMeta.setAlias(rsmd.getColumnLabel(i));            // 别名
				columnMeta.setTypeOfSQL(rsmd.getColumnType(i));         // 列类型
				columnMeta.setTypeName(rsmd.getColumnTypeName(i));      // 列类型名
				int isNullable = rsmd.isNullable(i);                    // 列是否可空
				columnMeta.setNullable((ResultSetMetaData.columnNullable == isNullable));
				columnMeta.setLength(rsmd.getPrecision(i));             // 列长度
				columnMeta.setScale(rsmd.getScale(i));                  // 列精度

				columnMetaMap.put(columnName, columnMeta);
			}
			return columnMetaMap;
		} catch (Exception e) {
			throw new DBException(db.getID(), "sql [" + sql + "] column info failed", e);
		} finally {
			try {
				if (rsThis != null) rsThis.close();
			} catch (Exception e) {
			}
			try {
				if (pstmtThis != null) pstmtThis.close();
			} catch (Exception e) {
			}
		}
	}
}
