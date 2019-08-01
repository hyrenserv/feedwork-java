package ${basePackage}.${subPackage};

import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.netclient.http.HttpClient;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.web.action.ActionResult;
import test.yyy.testbase.WebBaseTestCase;

import ${basePackage}.entity.SysPara;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SysParaActionTest extends WebBaseTestCase {
	private static final String UATTableName = SysPara.TableName;
	private static final int Init_Rows = 10; // 向表中初始化的数据条数。
	/**
	 * 如果希望 login 仅仅执行一次：
	 * 1）给类添加注解： FixMethodOrder(MethodSorters.NAME_ASCENDING)
	 * 2）把Before改成Test
	 * 3）方法名用A0开头（保证字母顺序最小即可）
	 */
	@Before
	public void before() {
		// 初始化测试用例数据
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			List<Object[]> params = new ArrayList<>();
			for (int i = 0; i < Init_Rows; i++) {
				Object[] rowParams = new Object[]{"init-" + i, i};
				params.add(rowParams);
			}
			int[] nums = SqlOperator.executeBatch(db,
					"insert into " + UATTableName + "(para_name, para_value) values(?, ?)",
					params
			);
			assertThat("测试数据初始化", nums.length, is(Init_Rows));
			for (int i = 0; i < nums.length; i++)
				assertThat("initData : " + i, nums[i], is(1));
			SqlOperator.commitTransaction(db);
		}

		// 用户登录
		String responseValue = new HttpClient()
				.buildSession()
				.addData("username", "admin")
				.addData("password", "admin")
				.post(getUrlActionPattern() + "/" + AppinfoConf.AppBasePackage.replace(".", "/")
							+ "/biz/zauth/loginAtSession")
				.getBodyString();
		ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat("用户登录", ar.isSuccess(), is(true));

		TestCaseLog.println("Start One Testcase ... ...");
	}
	@After
	public void after() {
		TestCaseLog.println("Current TestCase Done, clean test data ... ...");
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			SqlOperator.execute(db,"delete from " + UATTableName);
			SqlOperator.commitTransaction(db);
			long nums = SqlOperator.queryNumber(db, "select count(1) from " + UATTableName)
					.orElseThrow(()->new RuntimeException("count fail!"));
			assertThat("整个表数据删除后，表记录数应该为0", nums, is(0L));
		}
	}
	@Test
	public void add() {
		String name = "uat-para";
		String value = "uat " + DateUtil.getDateTime();
		String responseValue = new HttpClient()
				.addData("para_name", name).addData("para_value", value)
				.post(getActionUrl("add"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat(ar.isSuccess(), is(true));

		// 验证DB里面的数据是否正确
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Map<String, Object> result = SqlOperator.queryOneObject(db,
					"select * from " + UATTableName + " where para_name=?", name);
			String newValue = (String)result.get("para_value");
			assertThat(value, is(newValue));
		}

		// 再次执行，应该因为主键冲突报异常
		responseValue = new HttpClient()
				.addData("para_name", name).addData("para_value", value)
				.post(getActionUrl("add"))
				.getBodyString();
		ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat(ar.isSuccess(), is(false));
	}

	@Test
	public void update() {
		String name = "init-0";
		String value = "v"+System.currentTimeMillis();
		String responseValue = new HttpClient()
				.addData("name", name).addData("value", value)
				.post(getActionUrl("update"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat(ar.isSuccess(), is(true));

		// 验证DB里面的数据是否正确
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Map<String, Object> result = SqlOperator.queryOneObject(db,
					"select * from " + UATTableName + " where para_name=?", name);
			String newValue = (String)result.get("para_value");
			assertThat(value, is(newValue));
		}
	}

	@Test
	public void delete() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			String name = "init-0";
			// 验证DB里面预期被删除的数据是存在的
			OptionalLong result = SqlOperator.queryNumber(db,
					"select count(1) from " + UATTableName + " where para_name=?", name);
			assertThat(result.orElse(Long.MIN_VALUE), is(1L)); // 被删除前所以为1

			// 业务处理
			String responseValue = new HttpClient()
					.addData("name", name)
					.post(getActionUrl("delete"))
					.getBodyString();
			ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
			assertThat(ar.isSuccess(), is(true));

			// 验证DB里面的数据是否正确
			result = SqlOperator.queryNumber(db,
					"select count(1) from " + UATTableName + " where para_name=?", name);
			assertThat(result.orElse(Long.MIN_VALUE), is(0L)); // 被删除了所以为0
		}
	}

	@Test
	public void getPagedDataList() {
		int pageSize = (Init_Rows/3); // 分3页
		String responseValue = new HttpClient()
				.addData("currPage", 2).addData("pageSize", pageSize)
				.post(getActionUrl("getPagedDataList"))
				.getBodyString();
		ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat(ar.isSuccess(), is(true));

		Map<String, Object> result = (Map<String, Object>)ar.getData();
		assertThat(responseValue, containsString("\"count\": "+Init_Rows)); // 总数据量
		List<Map<String, Object>> sysparaList = (List<Map<String, Object>>)result.get("sysparaList");
		Map<String, Object> row = sysparaList.get(0);
		// 第2页的第一条的值就是pageSize。因为初始数据是从0开始的
		assertThat(row.get("para_value").toString(), is(String.valueOf(pageSize)));
	}
}