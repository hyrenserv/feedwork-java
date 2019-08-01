package fd.ng.db.jdbc;

import fd.ng.db.DbBaseTestCase;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.resultset.Result;
import fd.ng.db.resultset.TooManyRecordsException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqlOperatorTest extends DbBaseTestCase {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	private static final String testTableName = "__testfd_17456";
	private static final int Init_Rows = 10; // 向表中初始化的数据条数。必须大于等于10，否则会导致后面的测试用例失败

	@BeforeClass
	public static void start() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			if (!db.isExistTable(testTableName))
				db.ExecDDL("create table " + testTableName + "( name varchar(20), age int)");
			SqlOperator.beginTransaction(db);
			assertThat("启动事务失败", db.isBeginTrans(), is(true));
		}
	}

	@Before
	public void before() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<Object[]> params = new ArrayList<>();
			for (int i = 0; i < Init_Rows; i++) {
				Object[] rowParams = new Object[]{"newUser" + i, i};
				params.add(rowParams);
			}
			int[] nums = SqlOperator.executeBatch(db,
					"insert into " + testTableName + "(name, age) values(?, ?)",
					params
			);
			assertThat("initData 失败", nums.length, is(Init_Rows));
			for (int i = 0; i < nums.length; i++)
				assertThat("initData 部分失败 : " + i, nums[i], is(1));
			SqlOperator.commitTransaction(db);
		}
	}
	@After
	public void cleanData() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db, "delete from " + testTableName);
			assertThat("整个表数据删除，被删除数据应该大于0", nums, greaterThan(0));
			SqlOperator.commitTransaction(db);
		}
	}
	@AfterClass
	public static void end() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			db.ExecDDL("drop table " + testTableName);
		}
	}

	@Test
	public void t11_insert() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db,
					"insert into " + testTableName + "(name, age) values(?, ?)",
					"insertUser1", -74561
			);
			assertThat(nums, is(1));
			nums = SqlOperator.execute(db,
					"insert into " + testTableName + "(name, age) values(?, ?)",
					"insertUser2", -74562
			);
			assertThat(nums, is(1));

			SqlOperator.commitTransaction(db);

			Result result = SqlOperator.queryResult(db,
					"select * from " + testTableName + " where age>=? and age<=? order by age",
					-74563, -74561
			);
			assertThat(result.getRowCount(), is(2));
			assertThat(result.getString(0, "name"), is("insertUser2"));
			assertThat(result.getInt(1, "age"), is(-74561));

			List<JdbcTestUser> beanList = SqlOperator.queryList(db, JdbcTestUser.class,
					"select * from " + testTableName + " where age>=? and age<=? order by age",
					-74563, -74561
			);
			assertThat(beanList.size(), is(2));
			assertThat(beanList.get(0).getName(), is("insertUser2"));
			assertThat(beanList.get(1).getAge(), is(-74561));
		}
	}

	@Test
	public void t21_update() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db,
					"update " + testTableName + " set age = age + 1 where age < ?",
					1
			);
			assertThat(nums, is(1));

			SqlOperator.commitTransaction(db);

			Result result = SqlOperator.queryResult(db,
					"select * from " + testTableName + " where age=?",
					1
			);
			assertThat(result.getRowCount(), is(2));
			assertThat(result.getString(0, "name") + ", " + result.getString(1, "name"),
					allOf(containsString("newUser0"), containsString("newUser1")));
		}
	}

	/**
	 * 测试事务处理是否正确
	 */
	@Test
	public void t81_transaction() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db,
					"update " + testTableName + " set age = age + 100 where age > 5");
			assertThat(nums, is(4));
			SqlOperator.rollbackTransaction(db);
			OptionalLong count = SqlOperator.queryNumber(db, "select count(1) from " + testTableName);
			assertThat("rollback 失败", count.getAsLong(), is((long) Init_Rows));

			// 添加两条数据
			nums = SqlOperator.execute(db,
					"insert into " + testTableName + "(name, age) values('xxx1', 800000000)");
			assertThat(nums, is(1));
			nums = SqlOperator.execute(db,
					"insert into " + testTableName + "(name, age) values('xxx2', 800000001)");
			assertThat(nums, is(1));
			SqlOperator.commitTransaction(db);
			count = SqlOperator.queryNumber(db, "select count(1) from " + testTableName);
			assertThat("提交事务失败，或出现其他问题", count.getAsLong(), is((long) Init_Rows + 2));

			// 删除5条数据
			nums = SqlOperator.execute(db,
					"delete from " + testTableName + " where age < ?", 5);
			assertThat(nums, is(5));
			SqlOperator.rollbackTransaction(db);
			count = SqlOperator.queryNumber(db, "select count(1) from " + testTableName);
			assertThat("rollback 失败", count.getAsLong(), is((long) Init_Rows + 2));
		}
	}

	@Test
	public void t31_delete() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db,
					"delete from " + testTableName + " where age = ? or (age > ? and age < ?)",
					0, 1, 4 // 删除了0, 2，3，共3条数据
			);
			assertThat(nums, is(3));
			SqlOperator.commitTransaction(db);
			OptionalLong count = SqlOperator.queryNumber(db, "select count(1) from " + testTableName);
			assertThat("提交事务失败，或出现其他问题", count.getAsLong(), is((long) Init_Rows - 3));
		}
	}

	// ------------------------------- 查询类测试 ------------------------------

	@Test
	public void t41_queryToListMap() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<Map<String, Object>> result = SqlOperator.queryList(db,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.size(), is(9));
			assertThat(result.get(0).get("name"), is("newUser9"));
			assertThat(result.get(1).get("age"), is(8));
		}
	}
	@Test
	public void t41_queryToPagedListMap() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Page page = new DefaultPageImpl();
			// 每页3条，查第1页
			page.setCurrPage(1);
			page.setPageSize(3);
			List<Map<String, Object>> result = SqlOperator.queryPagedList(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.size(), is(3));
			assertThat(result.get(0).get("name"), is("newUser1"));
			assertThat(result.get(2).get("age"), is(3));

			// 每页4条，查最后一页面
			page.setCurrPage(3);
			page.setPageSize(4);
			result = SqlOperator.queryPagedList(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.size(), is(1));
			assertThat(result.get(0).get("name"), is("newUser9"));
			assertThat(result.get(0).get("age"), is(9));

			// 设置超范围的数据：当前页为10000
			page.setCurrPage(10000);
			page.setPageSize(4);
			result = SqlOperator.queryPagedList(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.size(), is(0));
		}
	}

	@Test
	public void t42_queryToResult() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Result result = SqlOperator.queryResult(db,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.getRowCount(), is(9));
			assertThat(result.getString(0, "name"), is("newUser9"));
			assertThat(result.getInt(1, "age"), is(8));
		}
	}
	@Test
	public void t41_queryToPagedResult() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Page page = new DefaultPageImpl();
			// 每页3条，查第1页
			page.setCurrPage(1);
			page.setPageSize(3);
			Result result = SqlOperator.queryPagedResult(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.getRowCount(), is(3));
			assertThat(result.getString(0, "name"), is("newUser1"));
			assertThat(result.getInt(2, "age"), is(3));

			// 每页4条，查最后一页面
			page.setCurrPage(3);
			page.setPageSize(4);
			result = SqlOperator.queryPagedResult(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.getRowCount(), is(1));
			assertThat(result.getString(0, "name"), is("newUser9"));
			assertThat(result.getInt(0, "age"), is(9));

			// 设置超范围的数据：当前页为10000
			page.setCurrPage(10000);
			page.setPageSize(4);
			result = SqlOperator.queryPagedResult(db,
					page,
					"select * from " + testTableName + " where age>? and age<? order by age",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.getRowCount(), is(0));
		}
	}

	@Test
	public void t43_queryToArrayList() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<Object[]> result = SqlOperator.queryArrayList(db,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.size(), is(9));
			assertThat(result.get(0)[0], is("newUser9"));
			assertThat(result.get(1)[1], is(8));
		}
	}
	@Test
	public void t43_queryToBeanList() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<JdbcTestUser> result = SqlOperator.queryList(db, JdbcTestUser.class,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.size(), is(9));
			assertThat(result.get(0).getName(), is("newUser9"));
			assertThat(result.get(1).getAge(), is(8));
		}
	}
	@Test
	public void t41_queryToMap() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Map<String, Object> result = SqlOperator.queryOneObject(db,
					"select age,name from " + testTableName + " where age=? order by age desc",
					5
			);
			assertThat(result.size(), is(2));
			assertThat(result.get("name"), is("newUser5"));
			assertThat(result.get("age"), is(5));
		}
	}
	@Test
	public void t41_queryToBean() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Optional<JdbcTestUser> result = SqlOperator.queryOneObject(db, JdbcTestUser.class,
					"select age,name from " + testTableName + " where age=?",
					5
			);
			// 不提取 Opetional 总的对象，直接使用
			assertThat(result.map(JdbcTestUser::getName).get(), is("newUser5"));
			assertThat(result.map(JdbcTestUser::getAge).get(), is(5));
			// 提取对象后做处理
			JdbcTestUser user = result.orElseGet(JdbcTestUser::new); // 没有查询到数据，则会new出来一个新对象。注：orElseGet是延时调用的方式
			assertThat(user.getName(), is("newUser5"));
			assertThat(user.getAge(), is(5));

			// 测试查询不到数据
			Optional<JdbcTestUser> resultNon = SqlOperator.queryOneObject(db, JdbcTestUser.class,
					"select age,name from " + testTableName + " where age=?",
					123456789
			);
			assertThat(resultNon.map(JdbcTestUser::getName).orElse("no user"), is("no user"));
			JdbcTestUser userNon = resultNon.orElseGet(JdbcTestUser::new);
			assertThat(userNon.getName(), nullValue());
		}
	}

	// 测试是否抛出了这个异常
	@Test(expected = TooManyRecordsException.class)
	public void t41_queryToBeanMultiRow() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
//		expectedEx.expect(TooManyRecordsException.class);
//		expectedEx.expectMessage(containsString("There are multiple rows in ResultSet"));
			SqlOperator.queryOneObject(db, JdbcTestUser.class,
					"select age,name from " + testTableName + " where age>?",
					4
			);
		}
	}
	@Test
	public void t43_queryToArray() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			Object[] result = SqlOperator.queryArray(db,
					"select age,name from " + testTableName + " where age=? order by age desc",
					5
			);
			assertThat(result.length, is(2));
			assertThat(result[1], is("newUser5"));
			assertThat(result[0], is(5));
		}
	}

	@Test
	public void t43_queryToNumber() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			OptionalLong result = SqlOperator.queryNumber(db,
					"select count(1) as nums from " + testTableName + " where age<?",
					9
			);
			result.ifPresent(number -> {
				// 业务处理函数
			});
			long val = result.orElseThrow(() -> new RuntimeException("XXX"));
			assertThat(result.orElse(Long.MIN_VALUE), is(9L));
			// 测试查询不到数据
			result = SqlOperator.queryNumber(db,
					"select count(1) as nums from " + testTableName + " group by age"
			);
			assertThat(result.isPresent(), is(false));
		}
	}

	/**
	 * 验证数据查询的中文的获取是否正确
	 */
	@Test
	public void check_HanZi_InDB() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			int nums = SqlOperator.execute(db,
					"insert into " + testTableName + "(name, age) values('汉字检查', -17000)");
			assertThat("插入一行有汉字的数据，失败", nums, is(1));
			SqlOperator.commitTransaction(db);
			Map<String, Object> result = SqlOperator.queryOneObject(db,
					"select age,name from " + testTableName + " where age=-17000");
			//System.out.println("check_HanZi_InDB ： " + result.get("name"));
			assertThat("查询到的汉字和原汉字不相等", result.get("name"), is("汉字检查"));
		}
	}
}
