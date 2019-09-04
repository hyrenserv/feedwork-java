package fd.ng.web.hmfmswebapp.a0101;

import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.UuidUtil;
import fd.ng.db.entity.anno.Table;
import fd.ng.netclient.http.HttpClient;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.web.WebBaseTestCase;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * 对应 UserManagerAction
 */
public class UserManagerActionTest extends WebBaseTestCase {
	@Test
	public void session() {
		String responseValue = new HttpClient()
				.post(getActionUrl("session"))
				.getBodyString();
	}

	@Test
	public void testAnnoValue() {
		Table tableAnno = UserForTestTable.class.getAnnotation(Table.class);
		assertThat(tableAnno.tableName(), equalTo(UserForTestTable.TableName));
	}

	@Test
	public void perfAmountGetJdbcWraper() {
		String responseValue = new HttpClient()
				.post(getActionUrl("perfAmountGetJdbcWraper"))
				.getBodyString();
		long et = JsonUtil.toObjectByNodeName(responseValue, "data", Long.class);
		assertThat(et, lessThan(50L));
	}

	@Test
	public void testUsedDB() {
		String responseValue = new HttpClient()
				.post(getActionUrl("testUsedDB"))
				.getBodyString();
		boolean ret = JsonUtil.toObjectByNodeName(responseValue, "data", Boolean.class);
		assertThat(ret, is(true));
	}

	@Test
	public void postBigData() {
		String str = "";
		for(int i=0; i<5000; i++) {
			str += "1234567890";
		}
		TestCaseLog.println("post data length="+str.length());

		String responseValue = new HttpClient()
				.addData("name", str)
				.post(getActionUrl("welcome"))
				.getBodyString();
//		String responseValue = post(getActionUrl("welcome"), new String[][]{
//				{"name", str}
//		});
		assertThat(responseValue, containsString("person"));
	}

	@Test
	public void index() {
		String responseValue = new HttpClient().post(getActionUrl("index")).getBodyString();
//		String responseValue = post(getActionUrl("index"));
		ActionResult ar = ActionResultHelper.fromJson(responseValue);
		assertThat(ar.getData(), is("index"));
	}

	@Test
	public void welcome() {
//		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
//		requestParameter.put( "name", new String[]{"李四"} );
//		String responseValue = post(getActionUrl("welcome"), requestParameter);

		String responseValue = new HttpClient()
				.addData("name", "李四")
				.post(getActionUrl("welcome"))
				.getBodyString();

		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, allOf(containsString("person"), containsString("李四")));

		Person person = JsonUtil.toObjectByNodeName(ar, "person", Person.class);
		assertThat(person.getName(), is("李四"));
		assertThat(person.getAge(), is(99));
		assertThat(person.getFavors(), is(new String[]{"xxx", "yyy"}));
		assertThat(person.getAmt(), is(new BigDecimal("234.99")));
	}

	@Test
	public void bean() {
//		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
//		requestParameter.put( "name", new String[]{"李四"} );
//		requestParameter.put( "age", new String[]{"99"} );
//		requestParameter.put( "sex", new String[]{"男"} );
//		requestParameter.put( "favors", new String[]{"xxx", "yyy"} );
//		requestParameter.put( "money", new String[]{"234.99"} );
//
//		String responseValue = post(getActionUrl("bean"), requestParameter);

		String responseValue = new HttpClient()
				.addData("name", "李四")
				.addData("age", 99)
				.addData("sex", "男")
				.addData("favors", new String[]{"xxx", "yyy"})
				.addData("money", "234.99")
				.post(getActionUrl("bean"))
				.getBodyString();

		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, allOf( containsString("\"person\":"),
				containsString("\"name\":\"李四\""),
				containsString("\"age\":99"),
				containsString("\"sex\":\"男\""),
				containsString("\"favors\":[\"xxx\",\"yyy\"]"),
				containsString("\"amt\":234.99"),
				containsString("\"wife\":")));

		Person person = JsonUtil.toObjectByNodeName(ar, "person", Person.class);
		assertThat(person.getName(), is("李四"));
		assertThat(person.getAge(), is(99));
		assertThat(person.getFavors(), is(new String[]{"xxx", "yyy"}));
		assertThat(person.getAmt(), is(new BigDecimal("234.99")));
	}

	// ---------------  测试业务异常  ---------------

	@Test
	public void bizExcetion_OnlyMsg() {
		String responseValue = new HttpClient().post(getActionUrl("bizExcetion_OnlyMsg")).getBodyString();

		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"only msg\","));
	}
	@Test
	public void bizExcetion_ResNoArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizExcetion_ResNoArgs")).getBodyString();
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"User list!\","));
	}
	@Test
	public void bizExcetion_ResHasArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizExcetion_ResHasArgs")).getBodyString();
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"typeOfSQL of 用户(FD飞) is 123.\","));
	}

	// --------- 指定错误编码的异常 ---------
	@Test
	public void bizCodeExcetion_denyValue() {
		String responseValue = new HttpClient().post(getActionUrl("bizCodeExcetion_denyValue")).getBodyString();

		// 因为在总控中的异常处理段，再次抛出运行时异常，这就编程了系统错误，且不会以JSON格式返回前端
		assertThat(responseValue, containsString("HTTP ERROR 500"));
	}
	@Test
	public void bizCodeExcetion_OnlyMsg() {
		String responseValue = new HttpClient().post(getActionUrl("bizCodeExcetion_OnlyMsg")).getBodyString();

		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":1101,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"only msg\","));
	}
	@Test
	public void bizCodeExcetion_ResNoArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizCodeExcetion_ResNoArgs")).getBodyString();
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":1102,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"User list!\","));
	}
	@Test
	public void bizCodeExcetion_ResHasArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizCodeExcetion_ResHasArgs")).getBodyString();
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":1103,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"typeOfSQL of 用户(FD飞) is 123.\","));
	}

	// --------- 系统异常 ---------
	@Test
	public void bizSysExcetion_OnlyEx() {
		String responseValue = post(getActionUrl("bizSysExcetion_OnlyEx"));
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台是对捕获的异常直接包裹后抛出，没有提供自定义消息，所以，框架会统一返回前端： "Business Exception"
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"Business Exception | "));
	}
	@Test
	public void bizSysExcetion_MsgAndEx() {
		String responseValue = post(getActionUrl("bizSysExcetion_MsgAndEx"));
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端
		assertThat(responseValue.replace(": ", ":"), containsString("\"message\":\"MsgAndEx | "));
	}
	@Test
	public void bizSysExcetion_MsgAndLogAndEx() {
		String responseValue = post(getActionUrl("bizSysExcetion_MsgAndLogAndEx"));
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端。
		// 同时，看日志中是否打印了字符串： "logged MSG"
		assertThat(responseValue.replace(": ", ":"),
				containsString("\"message\":\"MsgAndLogAndEx | "));
	}

	// ---------------  测试有数据操作的功能  ---------------

	@Test
	public void addUser() {
		String name = UuidUtil.uuid();

		String responseValue = post(getActionUrl("addUser"), new String[][]{
				{"name", name}, {"age", "28"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));

		// 删除上面的用户
		String url = getActionPath() + "/delUser";
		responseValue = post( url, new String[][]{ {"name", name} } );
		ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	public void updateUser() {
		String name = UuidUtil.uuid();
		// 先添加
		String responseValue = post(getActionUrl("addUser"), new String[][]{
				{"name", name}, {"age", "-1"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));

		// 更新
		responseValue = post(getActionUrl("updateUser"), new String[][]{
				{"name", name}, {"age", "-28"}
		});
		ActionResult aro = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat(aro.isSuccess(), is(true));

		// 查询更新的信息
		responseValue = post(getActionUrl("getUser"), new String[][]{
				{"name", name}
		});
		ar = JsonUtil.getNodeValue(responseValue, "data");
		UserForTestTable user = JsonUtil.toObject(ar, UserForTestTable.class);
		assertThat(user.getName(), equalTo(name));
		assertThat(user.getAge(), is(-28));

		// 删除上面的用户
		String url = getActionPath() + "/delUser";
		responseValue = post( url, new String[][]{ {"name", name} } );
		ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	public void addUserByEntity() {
		String name = "entity:" + UuidUtil.uuid();

		String responseValue = post(getActionUrl("addUserByEntity"), new String[][]{
				{"name", name}, {"age", "88"}, {"password", "0"}, {"create_time", DateUtil.getDateTime()}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));

		// 删除上面的用户
		String url = getActionPath() + "/delUser";
		responseValue = post( url, new String[][]{ {"name", name} } );
		ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	public void getUser() {
		String responseValue = post(getActionUrl("getUser"), new String[][]{
				{"name", "lksdjf1"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, equalTo(""));

		responseValue = post(getActionUrl("getUser"), new String[][]{
				{"name", "aoot"}
		});
		ar = JsonUtil.getNodeValue(responseValue, "data");
		UserForTestTable user = JsonUtil.toObject(ar, UserForTestTable.class);
		assertThat(user.getName(), equalTo("aoot"));
		assertThat(user.getPassword(), equalTo("11111"));
	}

	@Test
	public void getAllUsersForMapList() {
		String responseValue = post(getActionUrl("getAllUsersForMapList"));
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
	}

	@Test
	public void getPagedUserResult() {
		String responseValue = post(getActionUrl("getPagedUserResult"), new String[][]{
				{"currPage", "1"}, {"pageSize", "5"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
		assertThat(ar, containsString("count"));
	}

	@Test
	public void getPagedUserResultNoCount() {
		String responseValue = post(getActionUrl("getPagedUserResultNoCount"), new String[][]{
				{"currPage", "1"}, {"pageSize", "5"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
		assertThat(ar, containsString("count"));
	}

	@Test
	public void getPagedUsers() {
		String responseValue = post(getActionUrl("getPagedUsers"), new String[][]{
				{"currPage", "1"}, {"pageSize", "5"}
		});
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
	}
}
