package fd.ng.web.hmfmswebapp.anno;

import fd.ng.core.utils.StringUtil;
import fd.ng.web.WebBaseTestCase;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 对应 ParamAction
 */
public class ParamActionTest extends WebBaseTestCase {
	private static final String WebApp_Base = "/fd/ng/web/hmfmswebapp";

	/**
	 * 不经过web，直接调用 action 方法的测试
	 */
	@Test
	public void testParamsPersonNoHttp() {
		ParamAction paramAction = new ParamAction();
		String data = paramAction.testParamsPerson(
				"name", null, -1, new Person(), new BigDecimal("-1.01"), "ignore");
		assertThat(data, data, containsString("name=name"));
		assertThat(data, data, containsString("age=-1"));
		assertThat(data, data, containsString("person="));
		assertThat(data, data, containsString("arg_money=-1.01"));
		assertThat(data, data, containsString("req_money=no_request"));
		assertThat(data, data, containsString("igore_info=ignore"));
	}

	@Test
	public void testParamsPerson() {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "name", new String[]{"testParamsPerson"} );
		requestParameter.put( "sex", new String[]{"male"} ); // sex允许空，但是给他值
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "money", new String[]{"10001.01"} );
		requestParameter.put( "igore_info", new String[]{"不应该有我！"} );

		String data = post(getActionUrl("testParamsPerson"), requestParameter);
		assertThat(data, containsString("name=testParamsPerson"));
		assertThat(data, containsString("sex=p"));
		assertThat(data, containsString("age=777"));
		assertThat(data, containsString("arg_money=10001.01"));
		assertThat(data, containsString("req_money=10001.01"));
		assertThat(data, containsString("person="));
		assertThat(data, containsString("amt=p10001.01"));
		assertThat(data, not(containsString("igore_info=")));
	}

	@Test
	public void testParamsBeanAndEntity() {
		String data = post(getActionUrl("testParamsBeanAndEntity"), new String[][]{
				{"name", "张三"}, {"age", "88"}, {"money", "99.9999"}
		});
		assertThat(data, containsString("FeedBean:张三"));
		assertThat(data, containsString("Entity:张三"));
		assertThat(data, containsString("name=张三, age=88"));
		assertThat(StringUtil.substringCount(data, "99.9999"), is(2));
	}

	/** 连续多次访问，查看运行性能 */
	@Ignore
	@Test
	public void testMultiPost() {
		for(int i=0; i<200; i++) {
			testParamsString();
		}
	}

	@Test
	public void testParamsString() {
		String data = runTest(getActionUrl("testParamsString"), "testParamsString");
		assertThat(data, data, containsString("agenull=null"));
		assertThat(data, data, containsString("agesnull=null"));
		assertThat(data, data, containsString("agedefault=1"));
		assertThat(data, data, containsString("agesdefault=[2, 3]"));
	}

	@Test
	public void testParamsInt() {
		String data = runTest(getActionUrl("testParamsInt"), "testParamsInt");
		assertThat(data, data, containsString("agenull=0"));
		assertThat(data, data, containsString("agesnull=[0, 0]"));
		assertThat(data, data, containsString("agedefault=1"));
		assertThat(data, data, containsString("agesdefault=[2, 3]"));
	}
	@Test
	public void testParamsInteger() {
		String data = runTest(getActionUrl("testParamsInteger"), "testParamsInteger");
		assertThat(data, data, containsString("agenull=null"));
		assertThat(data, data, containsString("agesnull=null"));
		assertThat(data, data, containsString("agedefault=1"));
		assertThat(data, data, containsString("agesdefault=[2, 3]"));
	}
	@Test
	public void testParamsBigDecimal() {
		String data = runTest(getActionUrl("testParamsBigDecimal"), "testParamsBigDecimal");
		assertThat(data, data, containsString("agenull=null"));
		assertThat(data, data, containsString("agesnull=null"));
		assertThat(data, data, containsString("agedefault=1"));
		assertThat(data, data, containsString("agesdefault=[2, 3]"));
	}

	private String runTest(String url, String name) {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "name", new String[]{name} );
		requestParameter.put( "sex", new String[]{"male"} );
		requestParameter.put( "sexes", new String[]{"男", "女"} );
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "ages", new String[]{"10", "11", "12"} );

		String data = post(url, requestParameter);
		//name=testParamsString, sex=male, sexes=[男, 女], age=777, agenull=null, agedefault=1, ages=[10, 11, 12], agesnull=null, agesdefault=[2, 3]

		assertThat(data, containsString("name="+name));
		assertThat(data, containsString("sex=male"));
		assertThat(data, containsString("sexes=[男, 女]"));
		assertThat(data, containsString("age=777"));
		assertThat(data, containsString("ages=[10, 11, 12]"));
		assertThat(data, containsString("agedefault=1"));
		assertThat(data, containsString("agesdefault=[2, 3]"));
		return data;
	}
}
