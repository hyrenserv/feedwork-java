package fd.ng.web.hmfmswebapp.anno;

import fd.ng.web.WebBaseTestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 用于测试 Params 注解的用例。配套的 Action 是： ParamsAction.java
 * 这种方式已经废弃
 * 如果要用这种方式，需把 WebServlet 中注释掉的 autowireParameters 更换为 assignParameters
 */
@Ignore
@Deprecated
public class ParamsActionTest extends WebBaseTestCase {
	private static final String WebApp_Base = "/fd/ng/web/hmfmswebapp";
	@Test
	public void testParamsRequest() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParamsRequest"} );
		requestParameter.put( "sex", new String[]{"male"} ); // sex允许空，但是给他值
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "money", new String[]{"10001.01"} );

		String data = post(getActionUrl("testParamsRequest"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsRequest"), not(containsString("sex"))
				,containsString("age=777")
				,containsString("arg_money=null"),containsString("req_money=10001.01")
		));
	}
	@Test
	public void testParamsPerson() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "name", new String[]{"testParamsPerson"} );
		requestParameter.put( "sex", new String[]{"male"} ); // sex允许空，但是给他值
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "money", new String[]{"10001.01"} );

		String data  = post(getActionUrl("testParamsPerson"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsPerson"), containsString("sex=p")
				,containsString("age=777")
				,containsString("arg_money=null"),containsString("req_money=10001.01")
				,containsString("person="),containsString("money=p10001.01")
		));
	}
	@Test
	public void testParamsString_int() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParamsString_int"} );
		//requestParameter.put( "sex", null );
		requestParameter.put( "addr", new String[]{"北京西路95号", "天山路8号", "华光路168号xxx"} );
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "ages", new String[]{"11", "22", "33"} );

		String data = post(getActionUrl("testParamsString_int"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsString_int"), containsString("sex=null")
				,containsString("北京西路95号"), containsString("天山路8号"), containsString("华光路168号xxx")
				,containsString("age=777"), containsString("ages=")
				,containsString("11"),containsString("22"), containsString("33")
		));
	}
	@Test
	public void testParamsInteger() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParamsInteger"} );
		requestParameter.put( "sex", new String[]{"male"} ); // sex允许空，但是给他值
		requestParameter.put( "age", new String[]{"777"} );
		requestParameter.put( "ages", new String[]{"11", "22", "33"} );

		String data = post(getActionUrl("testParamsInteger"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsInteger"), containsString("sex=male")
				,containsString("age=777"), containsString("noage=null"), containsString("ages=")
				,containsString("11"),containsString("22"), containsString("33")
		));
	}
	@Test
	public void testParams_long() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParams_long"} );
		requestParameter.put( "age", new String[]{"1549176965980"} );
		requestParameter.put( "ages", new String[]{"11", "1456176965957", "33"} );

		String data = post(getActionUrl("testParams_long"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParams_long")
				,containsString("age=1549176965980"), containsString("ages=")
				,containsString("11"),containsString("1456176965957"), containsString("33")
		));
	}
	@Test
	public void testParamsLong() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParamsLong"} );
		requestParameter.put( "age", new String[]{"1549176965980"} );
		requestParameter.put( "ages", new String[]{"11", "7456176965957", "33"} );

		String data = post(getActionUrl("testParamsLong"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsLong"), containsString("noage=null")
				,containsString("age=1549176965980"), containsString("ages=")
				,containsString("11"),containsString("7456176965957"), containsString("33")
		));
	}
	@Test
	public void testParamsBigDecimal() throws IOException {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put( "username", new String[]{"testParamsBigDecimal"} );
		requestParameter.put( "money", new String[]{"1549176965980.01"} );
		requestParameter.put( "nomoney", new String[]{} );
		requestParameter.put( "moneys", new String[]{"11", "7456176965957.99", "33"} );

		String data = post(getActionUrl("testParamsBigDecimal"), requestParameter);
		assertThat(data, data, allOf(
				containsString("name=testParamsBigDecimal"), containsString("nomoney=null")
				,containsString("money=1549176965980.01"), containsString("moneys=")
				,containsString("11"),containsString("7456176965957.99"), containsString("33")
		));
	}
}
