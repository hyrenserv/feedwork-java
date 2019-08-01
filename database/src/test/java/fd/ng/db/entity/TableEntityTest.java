package fd.ng.db.entity;

import fd.ng.db.DbBaseTestCase;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import org.junit.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TableEntityTest extends DbBaseTestCase {
	private static final String testTableName = "__TableEntityTest_fd_17456";
	private static final String testTableScript = "create table " + testTableName + "( " +
			"name varchar(25), age int" +
			", class varchar(10), create_date varchar(8)" +
			")";
	private static final int testDataInitRows = 10;
	@BeforeClass
	public static void beforeClass() { createTestTable(testTableName, testTableScript); }
	@Before
	public void before() { initTestTableData(testTableName, testDataInitRows); }
	@After
	public void after() { cleanTestTableData(testTableName); }
	@AfterClass
	public static void afterClass() { dropTestTable(testTableName); }

	@Test
	public void CRUD() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			final String username = "user_add-" + System.currentTimeMillis();

			// 先查询这个名字的实体，应该找不到
			Map<String, Object> conds = new HashMap<>();
			conds.put("name", username);
			Optional<TableEntityTestUser> userOp = EntityOperator.getEntity(db,
					TableEntityTestUser.class, conds);
			assertThat(userOp.isPresent(), is(false));

			// 新增一条数据
			TableEntityTestUser userNew = new TableEntityTestUser();
			userNew.setName(username);
			userNew.setAge(101); // 不设置这个值，看看会出什么异常！
			userNew.setUclass("add");
			userNew.setCreate_date(null);
			int nums = userNew.add(db);
			assertThat(nums, is(1));
			SqlOperator.commitTransaction(db);
			// 查询这条新增的记录是否存在
			userOp = EntityOperator.getEntity(db, TableEntityTestUser.class, conds);
			assertThat(userOp.map(TableEntityTestUser::getAge).orElse(-1), is(101));
			assertThat(userOp.map(TableEntityTestUser::getName).orElse(""), is(username));
			assertThat(userOp.map(TableEntityTestUser::getUclass).orElse(""), is("add"));
			assertThat(userOp.map(TableEntityTestUser::getCreate_date).isPresent(), is(false));

			// 更新上一条数据
			userNew.clearStatus();
			userNew.setAge(77);
			userNew.setUclass("update");
			nums = userNew.update(db);
			assertThat(nums, is(1));
			SqlOperator.commitTransaction(db);
			userOp = EntityOperator.getEntity(db, TableEntityTestUser.class, conds);
			assertThat(userOp.map(TableEntityTestUser::getAge).orElse(-1), is(77));
			assertThat(userOp.map(TableEntityTestUser::getName).orElse(""), is(username));
			assertThat(userOp.map(TableEntityTestUser::getUclass).orElse(""), is("update"));
			// 更新上一条数据，验证null
			userNew.clearStatus();
			userNew.setAge(77);
			userNew.setUclass(null);
			nums = userNew.update(db);
			assertThat(nums, is(1));
			SqlOperator.commitTransaction(db);
			userOp = EntityOperator.getEntity(db, TableEntityTestUser.class, conds);
			assertThat(userOp.map(TableEntityTestUser::getAge).orElse(-1), is(77));
			assertThat(userOp.map(TableEntityTestUser::getName).orElse(""), is(username));
			assertThat(userOp.map(TableEntityTestUser::getUclass).isPresent(), is(false));

			// 删除这条数据
			userNew.clearStatus();
			nums = userNew.delete(db);
			assertThat(nums, is(1));
			SqlOperator.commitTransaction(db);
			userOp = EntityOperator.getEntity(db, TableEntityTestUser.class, conds);
			assertThat(userOp.isPresent(), is(false));
		}
	}

	@Test
	public void clearAllData() {
		try(DatabaseWrapper db = new DatabaseWrapper()) {
			List<TableEntityTestUser> all = EntityOperator.getAllData(db, TableEntityTestUser.class);
			assertThat(all.size(), is(testDataInitRows));

			int nums = EntityOperator.clearAllData(db, TableEntityTestUser.class);
			SqlOperator.commitTransaction(db);

			all = EntityOperator.getAllData(db, TableEntityTestUser.class);
			assertThat(all.size(), is(0));
		}
	}
}
