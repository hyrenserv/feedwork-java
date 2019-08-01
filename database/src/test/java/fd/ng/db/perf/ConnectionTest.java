package fd.ng.db.perf;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.test.junit.TestCaseLog;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("全部应该手工执行并观察运行状况")
public class ConnectionTest {
	final String driver = "com.mysql.jdbc.Driver";
	final String url = "jdbc:mysql://localhost:3306/xxx?useCursorFetch:true";
	final String user = "root";
	final String pwd = "xxx123";

	// 测试连接池释放资源被重用后是否恢复了初始状态。而且，每个conn的值应该相同，因为连接池里面的连接只有一个
	@Test
	public void connPoolValidate() throws Exception {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driver);
		config.setJdbcUrl(url);
		config.setUsername(user);
		config.setPassword(pwd);
//		config.setMinimumIdle(10);
		config.setMaximumPoolSize(1);

		HikariDataSource ds = new HikariDataSource(config);
		TestCaseLog.println("[%3d] New DS : %s, Pool(Min, Max)=(%d, %d) %n", Thread.currentThread().getId(),
				ds,	config.getMinimumIdle(), config.getMaximumPoolSize());
		assertThat(config.getMinimumIdle(), is(1));
		assertThat(config.getMaximumPoolSize(), is(1));

		Connection conn = getConnection(ds);
		conn.setAutoCommit(false);
		TestCaseLog.println("[%3d] conn : %s, autoCommit=%s %n", Thread.currentThread().getId(),
				conn, conn.getAutoCommit());
		assertThat(conn.getAutoCommit(), is(false));
		conn.close();


		Connection conn1 = getConnection(ds);
		assertThat(conn1.getAutoCommit(), is(true));
		conn1.close();

		Connection conn2 = getConnection(ds);
		conn2.close();
	}
	private Connection getConnection(HikariDataSource ds) throws Exception {
		Connection conn = ds.getConnection();
		TestCaseLog.println("[%3d] conn : %s, autoCommit=%s %n", Thread.currentThread().getId(),
				conn, conn.getAutoCommit());
		return conn;
	}
	// -------------  测试各种连接DB的方式的性能
	@Test
	public void connection() throws Exception {
//		threadBenchConn("pool");
//		threadBenchConn("oldJdbc");
		threadBenchConn("fdcore");

		Thread.sleep(10);
	}
	private void benchConn(String type) throws Exception {
		System.out.println();
		System.out.println("===========> Start : " + type);
		HikariDataSource ds = getDS();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 20; i++) {
			Connection conn = null;
			if("pool".equals(type)) conn = getConnectionPool(getDS());
			else if("oldJdbc".equals(type)) conn = getConnectionJdbcOld();
			else if("newJdbc".equals(type)) conn = getConnectionJdbcNew();
			System.out.println("conn = " + conn);
//			Thread.sleep(1000);
			if(conn!=null) conn.close();
		}
		System.out.println("time : " + (System.currentTimeMillis() - start));
	}
	private void threadBenchConn(final String type) throws Exception {
		int nums = 50;
		CountDownLatch counter = new CountDownLatch(nums);
		ExecutorService executor = Executors.newFixedThreadPool(nums);
		final List<Future<Throwable>> results = new ArrayList<Future<Throwable>>(nums);

		final HikariDataSource ds;
		if("pool".equals(type)) ds = getDS();
		else if("fdcore".equals(type)) {
//			ClassUtil.loadClass(DbinfosConf.class.getName());
			DatabaseWrapper db = new DatabaseWrapper(); // 仅仅为了预先创建好连接池
			db.close();
			ds = null;
		}
		else ds = null;

		System.out.printf("%n=========== START : %s ===========%n", type);

		long start = System.currentTimeMillis();
		for (int i = 0; i < nums; i++) {
			results.add(executor.submit(new Callable<Throwable>() {
				@Override
				public Throwable call() throws Exception {
					try {
						Connection conn = null;
						long startGetConn = System.currentTimeMillis();
						if("pool".equals(type)) conn = getConnectionPool(ds);
						else if("oldJdbc".equals(type)) conn = getConnectionJdbcOld();
						else if("newJdbc".equals(type)) conn = getConnectionJdbcNew();
						else if("fdcore".equals(type)) conn = getConnectionByFdcore();
						System.out.printf("[%3d] get one conn. getTime=%5d, id= %s%n", Thread.currentThread().getId(), (System.currentTimeMillis()-startGetConn), conn);
						ThreadLocalRandom random = ThreadLocalRandom.current();
						Thread.sleep(random.nextInt(6));
						if(conn!=null) conn.close();
						counter.countDown();
						return null;
					} catch (final Throwable t) {
						return t;
					}
				}
			}));
		}
		counter.await();
		System.out.println("time : " + (System.currentTimeMillis() - start));
	}
	private HikariDataSource getDS() {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driver);
		config.setJdbcUrl(url);
		config.setUsername(user);
		config.setPassword(pwd);
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(20);

		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 64); // 250
		config.addDataSourceProperty("prepStmtCacheSqlLimit", 512); // 2048

		HikariDataSource ds = new HikariDataSource(config);
		System.out.printf("[%3d] New DS : %s, Pool(Min, Max)=(%d, %d) %n", Thread.currentThread().getId(), ds,
				config.getMinimumIdle(), config.getMaximumPoolSize());
		return ds;
	}
	private Connection getConnectionPool(HikariDataSource ds) throws SQLException {

		Connection conn = ds.getConnection();
		return conn;
	}

	private Connection getConnectionJdbcOld() throws SQLException, ClassNotFoundException {
		Class.forName(driver);
		return DriverManager.getConnection(url, user, pwd);
	}

	private Connection getConnectionJdbcNew() throws SQLException, ClassNotFoundException {
		Class.forName(driver);
		return DriverManager.getConnection(url, user, pwd);
	}

	private Connection getConnectionByFdcore() throws SQLException, ClassNotFoundException {
		DatabaseWrapper db = new DatabaseWrapper();
		return db.getConnection();
	}
}
