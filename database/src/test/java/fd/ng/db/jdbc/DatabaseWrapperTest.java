package fd.ng.db.jdbc;

import fd.ng.db.DbBaseTestCase;
import fd.ng.db.conf.ConnWay;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.nature.PagedSqlInfo;
import fd.ng.db.resultset.processor.MapListProcessor;
import org.junit.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class DatabaseWrapperTest extends DbBaseTestCase {
	private static final String testTableName = "__DatabaseWrapperTest_fd17456_";
	@BeforeClass
	public static void start() {
		try (DatabaseWrapper db = new DatabaseWrapper.Builder().showsql(false).create() ) {
			assertThat(db.getName(), equalTo(DbinfosConf.DEFAULT_DBNAME));

			if (!db.isExistTable(testTableName))
				db.ExecDDL("create table " + testTableName + "(" +
					"  name varchar(48) primary key" +
					", age int not null" +
					", create_date char(8)" +
					", money decimal(16, 2)" +
					", status char(1) default '0'" +
					")");
			assertThat(db.isExistTable(testTableName), is(true));
		}
	}
	@AfterClass
	public static void end() {
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().showsql(false).create();) {
			db.ExecDDL("drop table " + testTableName);
		}
	}

	@After
	public void cleanData() {
		cleanTestTableData(testTableName);
	}

	// 测试第二个DB连接
	@Ignore("测试第二个DB连接，需要手工造数据后观察日志")
	@Test
	public void testOtherDB() throws SQLException {
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().dbname("pgsql").create();
		    ResultSet rs = db.queryPagedGetResultSet("select * from test", 2, 4, true);){
			while(rs.next())
				System.out.printf("uid=%s, name=%s %n", rs.getString("uid"), rs.getString("name"));
		}
	}

	// 测试 DatabaseWrapper 的各种创建方式
	@Test
	public void create() {
		try(DatabaseWrapper db0 = new DatabaseWrapper();
		    DatabaseWrapper db1 = new DatabaseWrapper.Builder().create();){

			assertThat(db0.toString(), containsString("autoCommit=true"));
			assertThat(db0.toString(), containsString("No Transaction"));
			assertThat(db0.getName(), equalTo(db1.getName()));
			assertThat(db0.getID(), not(equalTo(db1.getID())));
			String id = db1.getID();
			try(DatabaseWrapper db2 = new DatabaseWrapper.Builder().id(id).create();){
				assertThat(db0.getName(), equalTo(db2.getName()));
				assertThat(db0.getID(), not(equalTo(db2.getID())));
				assertThat(db1.getName(), equalTo(db2.getName()));
				assertThat(db1.getID(), equalTo(db2.getID()));
			}
		}

		try(DatabaseWrapper db =  new DatabaseWrapper.Builder().desc("[test desc]").lazyConnect(true).create()){
			assertThat("测试：设置描述信息", db.toString(), containsString("[test desc]"));
		}

		// 测试 lazy 时，dbinfo中要设置成 JDBC 连接方式。因为POOL方式没有所谓 lazy ，在启动时已经初始创建了N个连接
		try(DatabaseWrapper db =  new DatabaseWrapper.Builder().desc("[test dbLazy]").lazyConnect(true).create()){
			assertThat("测试：dbLazy方式创建", db.toString(), containsString("db connection is null."));
			db.makeConnection();
			assertThat("测试：dbLazy方式创建后，再启动事务", db.toString(), containsString("conn's autoCommit=true"));
		}

		try(DatabaseWrapper db = new DatabaseWrapper.Builder().desc("[test dbTrans]").autoCommit(false).create()){
			assertThat("测试：设置事务", db.toString(), containsString("conn's autoCommit=false, used transaction"));
		}

		DbinfosConf.Dbinfo dbinfo = new DbinfosConf.Dbinfo();
		dbinfo.setName(DbinfosConf.DEFAULT_DBNAME);
		dbinfo.setDriver("com.mysql.jdbc.Driver");
		dbinfo.setUrl("jdbc:mysql://localhost:3306/xxx?useCursorFetch:true");
		dbinfo.setUsername("root");
		dbinfo.setPassword("");
		dbinfo.setWay(ConnWay.JDBC);
		dbinfo.setDbtype(Dbtype.MYSQL);
		dbinfo.setShow_conn_time(true);
		dbinfo.setShow_sql(true);
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().dbconf(dbinfo).create()){
			assertThat(db.toString(), containsString("mysql"));
			assertThat(db.toString(), containsString("conn's autoCommit=true, No Transaction"));
		}
	}

	@Test
	public void testCloseShowsql() {
		String prefixName = "forCloseShowsqlName";
		DatabaseWrapper dbNoShowsql = new DatabaseWrapper.Builder().desc("[test dbNoShowsql]").showsql(false).create();
		int nums = -1;
		nums = dbNoShowsql.execute("insert into " + testTableName + "(name, age) values(?, ?)", prefixName, 11);
		assertThat(nums, is(1));
		List<Map<String, Object>> rsList = dbNoShowsql.query("select * from " + testTableName + " where name like ?",
				new MapListProcessor(), prefixName+"%");
		assertThat(rsList.size(), is(1));
		dbNoShowsql.close();
	}

	@Test
	public void query() {
		try(DatabaseWrapper db = new DatabaseWrapper();) {

			int nums = -1;
			nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryName1", 11);
			assertThat(nums, is(1));
			nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryName2", 12);
			assertThat(nums, is(1));
			nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryNone1", 13);
			assertThat(nums, is(1));
			nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryNone2", 14);
			assertThat(nums, is(1));

			List<Map<String, Object>> rsList = db.query("select * from " + testTableName + " where name like ?",
					new MapListProcessor(), "forQueryName%");
			assertThat(rsList.size(), is(2));

			// ----------- 以下为测试出入 List 类型的占位参数
			List<Object> params = new ArrayList<>();
			params.add("forQueryName%");
			rsList.clear();
			rsList = db.query("select * from " + testTableName + " where name like ?",
					new MapListProcessor(), params);
			assertThat("使用List作参数测试失败", rsList.size(), is(2));

			params.clear();
			params.add("forQueryN%");
			params.add(11);
			params.add(12);
			params.add(13);
			rsList.clear();
			rsList = db.query("select * from " + testTableName + " where name like ? and age in (?,?,?)",
					new MapListProcessor(), params);
			assertThat("使用List作参数测试失败", rsList.size(), is(3));

		}
	}

	@Test
	public void queryPaged() {
		DatabaseWrapper db = new DatabaseWrapper.Builder().showsql(false).create();
		final int total = 100;
		for(int i=0; i<total; i++) {
			int nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryPagedName" + i, i);
			assertThat(nums, is(1));
		}
		db.close();

		db = new DatabaseWrapper.Builder().create();
		// 取前10条
		MapListProcessor mapListProcessor = new MapListProcessor();
		List<Map<String, Object>> rsList = db.queryPaged(
				"  select * from " + testTableName + " where name like ? and age >= ? order by age",
				1, 10,
				mapListProcessor, "forQueryPagedName%", 0);
		assertThat(db.getCounts(), is(total));
		assertThat(rsList.size(), is(10));
		assertThat(rsList.get(0).get("age"), is(0));
		assertThat(rsList.get(9).get("age"), is(9));

		// 取 23-48条
		rsList = db.queryPaged(
				"  select * from " + testTableName + " where name like ? order by age",
				23, 48,
				new MapListProcessor(), "forQueryPagedName%");
		assertThat(db.getCounts(), is(total));
		assertThat(rsList.size(), is(26));
		assertThat(rsList.get(0).get("age"), is(22));
		assertThat(rsList.get(24).get("age"), is(46));

		db.close();
	}

	// 测试不计算总数据量的查询
	@Test
	public void queryPagedNoCount() {
		final int total = 100;
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().showsql(false).create()) {
			for (int i = 0; i < total; i++) {
				int nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", "forQueryPagedName" + i, i);
				assertThat(nums, is(1));
			}
		}
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().create()) {
			// 取前10条
			MapListProcessor mapListProcessor = new MapListProcessor();
			List<Map<String, Object>> rsList = db.queryPaged(
					"  select * from " + testTableName + " where name like ? and age >= ? order by age",
					1, 10, false,
					mapListProcessor, "forQueryPagedName%", 0);
			assertThat(db.getCounts(), not(is(total)));
			assertThat(rsList.size(), is(10));
			assertThat(rsList.get(0).get("age"), is(0));
			assertThat(rsList.get(9).get("age"), is(9));

			// 取 23-48条
			rsList = db.queryPaged(
					"  select * from " + testTableName + " where name like ? order by age",
					23, 48, false,
					new MapListProcessor(), "forQueryPagedName%");
			assertThat(db.getCounts(), not(is(total)));
			assertThat(rsList.size(), is(26));
			assertThat(rsList.get(0).get("age"), is(22));
			assertThat(rsList.get(24).get("age"), is(46));
		}
	}

	// 测试是否对没有提交/回滚的事务，在关闭前进行了清理（自动回滚）
	@Test
	public void testClearTransaction() {
		// 验证逻辑：启动事务后，插入、修改、删除数据，然后执行close，再连接上DB查询数据，看是否被回滚了。
		// 同时，查看日志，看有没有日志输出：
		String prefixName = "forClearTransName";
		DatabaseWrapper db = new DatabaseWrapper.Builder().autoCommit(false).create();
		int nums = -1;
		nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", prefixName+1, 11);
		assertThat(nums, is(1));
		nums = db.execute("insert into " + testTableName + "(name, age) values(?, ?)", prefixName+2, 12);
		assertThat(nums, is(1));
		db.close();
		// 验证是否插入了两条
		db = new DatabaseWrapper.Builder().create();
		List<Map<String, Object>> rsList = db.query("select * from " + testTableName + " where name like ?",
				new MapListProcessor(), prefixName+"%");
		assertThat(rsList.size(), is(0));
		db.close();
	}

	@Test
	public void easyUse() {
		String name = "aaa";
		int age     = 23;
		try (DatabaseWrapper db = new DatabaseWrapper();
		     ResultSet rs = db.queryGetResultSet("select * from user where name=? and age=?",
						name, age)) {
			while (rs.next()) {
				System.out.println("name=" + rs.getString("name"));
				System.out.println("age =" + rs.getInt("age"));
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	// ------------------- 以下是需要手工执行的测试用例 -------------------

	@Ignore("验证枚举方法访问的性能")
	@Test
	public void perfDbtype() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		int count = 10000;
		boolean isNew = true;

		Dbtype natureDB = Dbtype.ORACLE;
		long start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			PagedSqlInfo pagesql = natureDB.ofPagedSql("select * from xxx", 1, 100);
			String sql = pagesql.getSql();
			int begin  = pagesql.getPageNo1();
			int end    = pagesql.getPageNo2();
			sql        = natureDB.ofCountSql("select * from xxx");
			int j = 0;
		}
		long end = System.currentTimeMillis();
		System.out.printf("[%12s] time : %d %n", natureDB.name(), (end - start));
	}

	// 构造海量数据，测试 resultset 循环读数据的效率
	// create table HugeDataTable(name varchar(48), age int)
	@Ignore("构造海量数据，测试 resultset 循环读数据的效率")
	@Test
	public void hugeDataCreate() {
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().desc("[海量数据查询测试]").showsql(false).create();){
			String hugeTestTableName = "HugeDataTable";
			db.beginTrans();
			long start = System.currentTimeMillis();
			for (int i = 1; i < 1_000_001; i++) {
				int nums = db.execute("insert into " + hugeTestTableName + "(name, age) values(?, ?)", "hdex:" + i, i);
				if (i % 2000 == 0)
					db.commit();
			}
			db.commit();
			System.out.println("common exec time : " + (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			List<Object[]> listParams = new ArrayList<>(2000);
			for (int i = 1; i < 1_000_001; i++) {
				listParams.add(new Object[]{"hdbt:" + i, i});
				if (i % 2000 == 0) {
					db.execBatch("insert into " + hugeTestTableName + "(name, age) values(?, ?)", listParams);
					db.commit();
					listParams.clear();
				}
			}
			db.commit();
			System.out.println("batch  exec time : " + (System.currentTimeMillis() - start));
			// batch 在pgsql中快了几倍，在mysql中没什么变化
		}
	}
	@Ignore("原生JDBC查询大数量，观察 ResultSet.TYPE_FORWARD_ONLY 等参数的效果")
	@Test
	public void hugeDataQuery() {
		String hugeTestTableName = "HugeDataTable";
		DatabaseWrapper db = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			db = new DatabaseWrapper();
			conn = db.getConnection();
			String sql = "select * from " + hugeTestTableName;
			pstmt = conn.prepareStatement(sql
					, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
			);

			pstmt.setFetchSize(200); // mysql 这个参数造成后面的while循环非常慢
			long start = System.currentTimeMillis();
			rs = pstmt.executeQuery(); // mysql 约7秒左右
			System.out.println("===> query time : " + (System.currentTimeMillis() - start));

			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) { // mysql 整个循环约170毫秒左右
				if(i==100_000)
					System.out.println("===> 100_000 time : " + (System.currentTimeMillis() - start));
				i++;
			}
			System.out.println("===> total time : " + (System.currentTimeMillis() - start));
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{ if(rs!=null) rs.close(); }catch (Exception e) {e.printStackTrace();}
			try{ if(pstmt!=null) pstmt.close(); }catch (Exception e) {e.printStackTrace();}
			if(db!=null) db.close();
		}
	}

	@Ignore("测试对 PreparedStatement 的使用")
	@Test
	/**
	 * 测试对 PreparedStatement 的使用。
	 * 在整体测试集合中，本函数不需要被执行
	 */
	public void testPstmt() {
		DatabaseWrapper db = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			db = new DatabaseWrapper();
			conn = db.getConnection();
			String sql = "select count(1) as count from user where age > ?";
			pstmt = conn.prepareStatement(sql);

			pstmt.setFetchSize(200);
			pstmt.setObject(1, 0);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				System.out.println("count : " + rs.getInt(1));
			}
			rs.close();

			pstmt.setFetchSize(200);
			pstmt.setObject(1, 100);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				System.out.println("count : " + rs.getInt(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{ if(rs!=null) rs.close(); }catch (Exception e) {e.printStackTrace();}
			try{ if(pstmt!=null) pstmt.close(); }catch (Exception e) {e.printStackTrace();}
			if(db!=null) db.close();
		}
	}
}
