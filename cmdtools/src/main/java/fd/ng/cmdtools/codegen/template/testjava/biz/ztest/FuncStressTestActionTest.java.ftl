package ${basePackage}.${subPackage};

import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.UuidUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netclient.http.SubmitMediaType;
import fd.ng.test.junit.ExtendBasalRunner;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.test.junit.rules.anno.Parallel;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import test.yyy.testbase.WebBaseTestCase;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ExtendBasalRunner.class)
public class FuncStressTestActionTest extends WebBaseTestCase {
	// 以下的登陆处理方式，是每个方法都被调用一次。
	// 如果希望对整个类只跑登陆，可以打开上面FixMethodOrder注释，并且把本方法的注解该为 Test
	@Before
	public void A0login() {
		// 用户登录
		String responseValue = new HttpClient()
				.buildSession()
				.addData("username", "admin")
				.addData("password", "admin")
				.post(getUrlActionPattern() + "/" + AppinfoConf.AppBasePackage.replace(".", "/")
							+ "/biz/zauth/loginAtSession"    // 根据项目实际情况修改本链接
				)
				.getBodyString();

		ActionResult ar = JsonUtil.toObject(responseValue, ActionResult.class);
		assertThat("用户登录", ar.isSuccess(), is(true));
		TestCaseLog.println("FuncStressTestActionTest start ... ...");
	}

	@Test
	@Parallel
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
		assertThat(responseValue, containsString("person"));
	}

	@Test
	@Parallel
	public void perfAmountGetJdbcWraper() {
		String responseValue = new HttpClient()
				.post(getActionUrl("perfAmountGetJdbcWraper"))
				.getBodyString();
		long et = JsonUtil.toObjectByNodeName(responseValue, "data", Long.class);
		assertThat(et, lessThan(50L));
	}

	@Test
	@Parallel
	public void testUsedDB() {
		String responseValue = new HttpClient()
				.post(getActionUrl("testUsedDB"))
				.getBodyString();
		boolean ret = JsonUtil.toObjectByNodeName(responseValue, "data", Boolean.class);
		assertThat(ret, is(true));
	}

	@Test
	@Parallel
	public void welcome() {
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
	@Parallel
	public void bean() {
		String responseValue = new HttpClient()
				.addData("name", "李四")
				.addData("age", 99)
				.addData("sex", "男")
				.addData("favors", new String[]{"xxx", "yyy"})
				.addData("money", "234.99")
				.post(getActionUrl("bean"))
				.getBodyString();

		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("{\"name\":\"李四\",\"age\":99,\"sex\":\"男\",\"favors\":[\"xxx\",\"yyy\"],\"amt\":234.99,\"wife\":{\"husbandName\":\"李四\",\"age\":99,\"sex\":\"男\"}}"));

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

		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端
		assertThat(responseValue, containsString("\"message\": \"only msg\","));
	}
	@Test
	public void bizExcetion_ResNoArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizExcetion_ResNoArgs")).getBodyString();
		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue, containsString("\"message\": \"用例列表 !\","));
	}
	@Test
	public void bizExcetion_ResHasArgs() {
		String responseValue = new HttpClient().post(getActionUrl("bizExcetion_ResHasArgs")).getBodyString();
		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台抛出了异常时，使用的是资源文件中的内容，框架会把这个消息返回前端
		assertThat(responseValue, containsString("\"message\": \"用户(FD飞)的类型是：123\","));
	}

	@Test
	public void bizSysExcetion_OnlyEx() {
		String responseValue = new HttpClient().post(getActionUrl("bizSysExcetion_OnlyEx")).getBodyString();
		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台是对捕获的异常直接包裹后抛出，没有提供自定义消息，所以，框架会统一返回前端： "Business Exception"
		assertThat(responseValue, containsString("\"message\": \"Business Exception\","));
	}
	@Test
	public void bizSysExcetion_MsgAndEx() {
		String responseValue = new HttpClient().post(getActionUrl("bizSysExcetion_MsgAndEx")).getBodyString();
		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端
		assertThat(responseValue, containsString("\"message\": \"MsgAndEx\","));
	}
	@Test
	public void bizSysExcetion_MsgAndLogAndEx() {
		String responseValue = new HttpClient().post(getActionUrl("bizSysExcetion_MsgAndLogAndEx")).getBodyString();
		assertThat(responseValue, containsString("\"code\": 220,"));
		// 因为后台抛出了异常是带有自定义提示消息的，框架会把这个消息返回前端。
		// 同时，看日志中是否打印了字符串： "logged MSG"
		assertThat(responseValue, containsString("\"message\": \"MsgAndLogAndEx\","));
	}

	// ---------------  测试有数据操作的功能  ---------------

	@Test
	@Parallel
	public void addUser() {
		String name = UuidUtil.uuid();

		String responseValue = new HttpClient()
				.addData("name", name)
				.addData("age", 28)
				.post(getActionUrl("addUser"))
				.getBodyString();

		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));

		// 删除上面的用户
		String url = getActionPath() + "/delUser";
		responseValue = new HttpClient()
        				.addData("name", name)
        				.post(url)
        				.getBodyString();

		ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	@Parallel
	public void addUserByEntity() {
		String name = "entity:" + UuidUtil.uuid();

		String responseValue = new HttpClient()
				.addData("name", name)
				.addData("age", 345)
				.addData("password", "addUserEt")
				.addData("create_time", DateUtil.getDateTime())
				.post(getActionUrl("addUserByEntity"))
				.getBodyString();

		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));

		// 删除上面的用户
		String url = getActionPath() + "/delUser";
		responseValue = new HttpClient()
        				.addData("name", name)
        				.post(url)
        				.getBodyString();
		ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	@Parallel
	public void updateUser() {
		String name = "aoot";
		String responseValue = new HttpClient()
				.addData("name", name)
				.addData("age", 88)
				.addData("password", "updateUser")
				.addData("create_time", DateUtil.getDateTime())
				.post(getActionUrl("updateUser"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(JsonUtil.toObject(ar, boolean.class), is(true));
	}

	@Test
	@Parallel
	public void getUser() {
		String responseValue = new HttpClient()
				.addData("name", "lksdjf1")
				.addData("age", 188)
				.addData("password", "00")
				.addData("create_time", DateUtil.getDateTime())
				.post(getActionUrl("getUser"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, equalTo(""));

		responseValue = new HttpClient()
				.addData("name", "aoot")
				.post(getActionUrl("getUser"))
				.getBodyString();
		ar = JsonUtil.getNodeValue(responseValue, "data");
		FuncStressTestEntity user = JsonUtil.toObject(ar, FuncStressTestEntity.class);
		assertThat(user.getName(), equalTo("aoot"));
	}

	@Test
	@Parallel
	public void getAllUsersForMapList() {
		String responseValue = new HttpClient()
				.post(getActionUrl("getAllUsersForMapList"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
	}

	@Test
	@Parallel
	public void getPagedUserResult() {
		String responseValue = new HttpClient()
				.addData("currPage", "1")
				.addData("pageSize", "5")
				.post(getActionUrl("getPagedUserResult"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
		assertThat(ar, containsString("count"));
	}

	@Test
	@Parallel
	public void getPagedUserResultNoCount() {
		String responseValue = new HttpClient()
				.addData("currPage", "1")
				.addData("pageSize", "5")
				.post(getActionUrl("getPagedUserResultNoCount"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
		assertThat(ar, containsString("count"));
	}

	@Test
	@Parallel
	public void getPagedUsers() {
		String responseValue = new HttpClient()
				.addData("currPage", "1")
				.addData("pageSize", "5")
				.post(getActionUrl("getPagedUsers"))
				.getBodyString();
		String ar = JsonUtil.getNodeValue(responseValue, "data");
		assertThat(ar, containsString("aoot"));
	}

	@Test
	public void uploadfiles() throws IOException {
		// 动态创建几个用于测试的文件
		String file1 = FileUtil.TEMP_DIR_NAME + "中文文件.txt";
		FileUtil.createFileIfAbsent(file1, DateUtil.getDateTime()+"\n  了解离开教室的风景\n\t234 sdf ...");
		String file2 = FileUtil.TEMP_DIR_NAME + "shell.sh";
		FileUtil.createFileIfAbsent(file2, DateUtil.getDateTime()+" shell");
		String file3 = FileUtil.TEMP_DIR_NAME + "programe.exe";
		FileUtil.createFileIfAbsent(file3, DateUtil.getDateTime()+" exe");

		// 上传
		String savedDir = "/tmp/uat/exa/uploaded/";
		String responseValue = new HttpClient(SubmitMediaType.MULTIPART)
				.addData("someValue1", "离开时大家发来看看").addData("someValue2", "209898")
				.addData("savedDir", savedDir)
				.addFile("uploadFiles", new String[]{file1, file2, file3})
				.post(getActionUrl("uploadfiles"))
				.getBodyString();
		assertThat(responseValue, containsString("\"code\": 200,"));
		assertThat(responseValue, containsString("离开时大家发来看看 | 209898 | "));
		assertThat(responseValue, containsString(".txt"));
		assertThat(responseValue, containsString(" | shell.sh"));
		assertThat(responseValue, containsString(" | programe.exe"));

		// 以下两行断言，仅适用于本机开发测试，如果是远程主机，要注释掉这两行。
		assertThat(Paths.get(savedDir+"shell.sh").toFile().exists(), is(true));
		assertThat(Paths.get(savedDir+"programe.exe").toFile().exists(), is(true));
	}
}
