package fd.ng.db;

import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.web.util.Dbo;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.test.junit.FdBaseTestCase;
import org.hamcrest.CoreMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * 数据库操作的测试用例，可继承本类，并且添加如下代码用户构造测试用数据表及数据：
 * <pre>
 * private static final String testTableName = "__TableEntityTest_fd_17456";
 * private static final String testTableScript = "create table " + testTableName + "( name varchar(20), age int)";
 * private static final int testDataInitRows = 10; // 初始插入的记录数
 * @BeforeClass
 * public static void beforeClass() { createTestTable(testTableName, testTableScript); }
 * @Before
 * public void before() { initTestTableData(testTableName, testDataInitRows); }
 * @After
 * public void after() { cleanTestTableData(testTableName); }
 * @AfterClass
 * public static void afterClass() { dropTestTable(testTableName); }
 *
 * 其中，testTableName定义的表名务必用一种非正常的名字，以免被自动清理掉。
 * </pre>
 */
public class DbBaseTestCase extends FdBaseTestCase {
	// 每个测试用例的 beforeClass 使用
	public static void createTestTable(String testTableName, String testTableScript) {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			if (db.isExistTable(testTableName)) {
				System.out.println("TestCase Abort! the table : [ " + testTableName + " ] exist, please check in DB.");
				System.exit(-1);
			}
			db.ExecDDL(testTableScript);
			assertThat(db.getName(), equalTo(DbinfosConf.DEFAULT_DBNAME));
		}
	}

	// 每个测试用例的 before 使用，也就是说，每个用例执行开始前，都会初始化相同的数据
	public void initTestTableData(String testTableName, int dataRows) {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<Object[]> params = new ArrayList<>();
			for (int i = 0; i < dataRows; i++) {
				Object[] rowParams = new Object[]{"newUser" + i, i};
				params.add(rowParams);
			}
			int[] nums = SqlOperator.executeBatch(db,
					"insert into " + testTableName + "(name, age) values(?, ?)",
					params
			);
			assertThat("initData 失败", nums.length, is(dataRows));
			for (int i = 0; i < nums.length; i++)
				assertThat("initData 部分失败 : " + i, nums[i], is(1));
			SqlOperator.commitTransaction(db);
		}
	}

	// 每个测试用例的 after 使用，也就是说，每个用例执行结束后，都会清空数据表
	public void cleanTestTableData(String testTableName) {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			SqlOperator.execute(db, "delete from " + testTableName);
			Dbo.commitTransaction(db);
			List<Object[]> all = Dbo.queryArrayList(db, "select * from " + testTableName);
			assertThat("整表删除后，应该查询到0条数据", all.size(), CoreMatchers.is(0));
		}
	}

	// 每个测试用例的 afterClass 使用
	public static void dropTestTable(String testTableName) {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			db.ExecDDL("drop table " + testTableName);
		}
	}
}
