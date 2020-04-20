package fd.ng.db.meta;

import fd.ng.db.conf.ConnWay;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import org.junit.Test;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class MetaOperatorTest {
	private static final String colTypeTestTableName = "_coltypetest_3254_";

	@Test
	public void getTablesWithColumns() {
		final String newTable = "__getTablesBaseInfo_fd_17456";
		try(DatabaseWrapper db = new DatabaseWrapper();) {
			if (!db.isExistTable(newTable))
				db.ExecDDL("create table " + newTable + "(" +
						"  fund varchar(20) primary key, amt decimal(16, 2) not null default 0)");
			// 针对不同DB，创建相应的涵盖各种字段类型的表
			if(db.getDbtype()== Dbtype.MYSQL)
				db.ExecDDL("create table " + colTypeTestTableName + "(" +
						"idint int, idbigint bigint," +
						"vdecimal decimal(12,2), vfloat float, vdouble double," +
						"vdate date, vtime time, vdatetime datetime, vtimestamp timestamp," +
						"vblob blob, vtext text," +
						"schar char(4), svarchar varchar(10)" +
						")");

			assertThat(db.isExistTable(colTypeTestTableName), is(true));

			List<TableMeta> tableMetas = MetaOperator.getTablesWithColumns(db);
			assertThat(tableMetas.size(), greaterThan(2));
			int found = 0;
			for(TableMeta meta : tableMetas) {
				String tableName = meta.getTableName();
				if(tableName.equalsIgnoreCase(newTable)) {
					found++;
					assertThat(meta.getColumnMeta("fund").getName(), equalTo("fund"));
					assertThat(meta.getColumnMeta("fund").getTypeOfSQL(), is(Types.VARCHAR));
					assertThat(meta.getColumnMeta("fund").isNullable(), is(false));
					assertThat(meta.getColumnMeta("fund").getLength(), is(20));
					assertThat(meta.getColumnMeta("amt").getTypeOfSQL(), is(Types.DECIMAL));
					assertThat(meta.getColumnMeta("amt").isNullable(), is(false));
					assertThat(meta.getColumnMeta("amt").getLength(), is(16));
					assertThat(meta.getColumnMeta("amt").getScale(), is(2));
					assertThat(meta.getPrimaryKeys().size(), is(1));
					assertThat(meta.getPrimaryKeys().contains("fund"), is(true));
				} else if(tableName.equalsIgnoreCase(colTypeTestTableName)) {
					found++;
				}
			}
			if(db.getDbtype()== Dbtype.MYSQL)
				assertThat(found, is(2));
			else
				assertThat(found, is(1));

			db.ExecDDL("drop table " + newTable);
			db.ExecDDL("drop table " + colTypeTestTableName);
		}
	}

	@Test
	public void getSqlColumnMeta() {
		try (DatabaseWrapper db = new DatabaseWrapper.Builder().create();) {
			final String testTableName = "__DBMetaTest_fd17456_";
			if (!db.isExistTable(testTableName))
				db.ExecDDL("create table " + testTableName + "(" +
						"  name varchar(48) primary key" +
						", age int not null" +
						", create_date char(8)" +
						", money decimal(16, 2)" +
						", status char(1) default '0'" +
						")");
			assertThat(db.isExistTable(testTableName), is(true));

			Map<String, ColumnMeta> meta = MetaOperator.getSqlColumnMeta(db, "select name, age, create_date cdate from " + testTableName);
//		println("VARCHAR=" + Types.VARCHAR + ", CAHR=" + Types.CHAR + ", NCHAR=" + Types.NCHAR + ", NVARCHAR=" + Types.NVARCHAR);
//		println("INTEGER=" + Types.INTEGER + ", BIGINT=" + Types.BIGINT + ", SMALLINT=" + Types.SMALLINT + ", TINYINT=" + Types.TINYINT);
//		println("DECIMAL=" + Types.DECIMAL + ", NUMERIC=" + Types.NUMERIC + ", FLOAT=" + Types.FLOAT + ", DOUBLE=" + Types.DOUBLE + ", REAL=" + Types.REAL);
//		println("DATE=" + Types.DATE + ", TIME=" + Types.TIME + ", TIMESTAMP=" + Types.TIMESTAMP + ", TIME_WITH_TIMEZONE=" + Types.TIME_WITH_TIMEZONE);
//		println("NULL=" + Types.NULL + ", BOOLEAN=" + Types.BOOLEAN + ", ARRAY=" + Types.ARRAY + ", OTHER=" + Types.OTHER);
//		println("meta : " + meta.toString());
			assertThat(meta.get("name").getName(), equalTo("name"));
			assertThat(meta.get("name").isNullable(), is(false));
			assertThat(meta.get("age").getName(), equalTo("age"));
			assertThat(meta.get("age").getTypeOfSQL(), is(Types.INTEGER));
			assertThat(meta.get("create_date").getName(), equalTo("create_date"));
			assertThat(meta.get("create_date").getAlias(), equalTo("cdate"));

			db.ExecDDL("drop table " + testTableName);
		}
	}
	@Test
	public  void getMeta(){
		DbinfosConf.Dbinfo dbInfo = new DbinfosConf.Dbinfo();
		dbInfo.setName(DbinfosConf.DEFAULT_DBNAME);
		dbInfo.setDriver("oracle.jdbc.OracleDriver");
		dbInfo.setUrl("jdbc:oracle:thin:@47.103.83.1:1521:HYSHF");
		dbInfo.setUsername("HYSHF");
		dbInfo.setPassword("hyshf");
		dbInfo.setWay(ConnWay.JDBC);
		//2、获取数据库类型
		Dbtype dbType = Dbtype.ORACLE;
		dbInfo.setDbtype(dbType);
		dbInfo.setShow_conn_time(true);
		dbInfo.setShow_sql(true);
		DatabaseWrapper db = new DatabaseWrapper.Builder().dbconf(dbInfo).create();
		List<TableMeta> tableMetas = MetaOperator.getTablesWithColumns(db);
	}
}