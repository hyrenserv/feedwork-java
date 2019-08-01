package fd.ng.web.util;

import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import fd.ng.web.util.beans.PersonForRequest;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class RequestUtilTest extends FdBaseTestCase {
	@Test
	public void buildBeanFromRequest() {
		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put("username", new String[]{"buildBeanFromRequest"});
		requestParameter.put("age", new String[]{"77"});
		requestParameter.put("ages", new String[]{"11", "22", "33"});
		requestParameter.put("sex", null);
		requestParameter.put("favors", new String[]{"足球", " 电影", "读书 "});
		requestParameter.put("money", new String[]{"98235.01"});
		requestParameter.put("addr", new String[]{"北京西路95号", "天山路8号", "华光路168号xxx"});
		// Bean 里面的一个 Bean
		requestParameter.put("oname", new String[]{"obuildBeanFromRequest"});
		requestParameter.put("oage", new String[]{"770"});
		requestParameter.put("oages", new String[]{"110", "220", "330"});
		requestParameter.put("osex", null);
		requestParameter.put("ofavors", new String[]{"o足球", " o电影", "o读书 "});
		requestParameter.put("omoney", new String[]{"982350.01"});

		PersonForRequest person = RequestUtil.buildBeanFromRequest(requestParameter, PersonForRequest.class);

		assertThat("RequestUtil.buildBeanFromRequest()", person.getName(), is("buildBeanFromRequest"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAge(), is(77));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAges().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAges()[1], is(22));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getSex(), is((String)null));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[0], is("足球"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[1], is(" 电影"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[2], is("读书 "));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getMoney(), is(new BigDecimal("98235.01")));

		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOname(), is("obuildBeanFromRequest"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOage(), is(770));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOages().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOages()[1], is(220));

		// 性能测试 ： 每次调用应该小于1ms，所以，循环10次要效率10ms
		long start = System.currentTimeMillis();
		for(int i=0; i<10; i++) {
			RequestUtil.buildBeanFromRequest(requestParameter, PersonForRequest.class);
		}
		long et = System.currentTimeMillis()-start;
		assertThat("RequestUtil.buildBeanFromRequest1 should be less than 5ms.", et, lessThan(15L));
	}

	@Ignore
	@Test
	public void buildBeanFromRequestPref() {
		// 下面这次调用：因为首次使用非常耗时，后面再调用就是毫秒级的速度了
		// 另外，如果首次用了其他类（比如 String），也会提交后续的性能，但是提高不如使用自己效果好
		long start = System.currentTimeMillis();
		RequestUtil.buildBeanFromRequest(new HashMap<>(), PersonForRequest.class);
		long et = System.currentTimeMillis()-start;
		TestCaseLog.println("RequestUtil.buildBeanFromRequest1() Init time : %d", et);

		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
		requestParameter.put("name", new String[]{"buildBeanFromRequest"});
		requestParameter.put("age", new String[]{"77"});
		requestParameter.put("ages", new String[]{"11", "22", "33"});
		requestParameter.put("sex", null);
		requestParameter.put("favors", new String[]{"足球", " 电影", "读书 "});
		requestParameter.put("money", new String[]{"98235.01"});
		requestParameter.put("addr", new String[]{"北京西路95号", "天山路8号", "华光路168号xxx"});

		requestParameter.put("oname", new String[]{"obuildBeanFromRequest"});
		requestParameter.put("oage", new String[]{"770"});
		requestParameter.put("oages", new String[]{"110", "220", "330"});
		requestParameter.put("osex", null);
		requestParameter.put("ofavors", new String[]{"o足球", " o电影", "o读书 "});
		requestParameter.put("omoney", new String[]{"982350.01"});

		// 同时测试了构建时间
		for(int i=0; i<10; i++) {
//		buildBeanFromRequest0(requestParameter);
			buildBeanFromRequest1(requestParameter);
		}
	}

	public void buildBeanFromRequest1(Map<String, String[]> requestParameter) {
		long start = System.currentTimeMillis();
		PersonForRequest person = RequestUtil.buildBeanFromRequest(requestParameter, PersonForRequest.class);
		long et = System.currentTimeMillis()-start;
		assertThat("RequestUtil.buildBeanFromRequest1()", et, lessThan(50L));
		TestCaseLog.println("RequestUtil.buildBeanFromRequest1() deal time : %d", et);

		assertThat("RequestUtil.buildBeanFromRequest()", person.getName(), is("buildBeanFromRequest"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAge(), is(77));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAges().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getAges()[1], is(22));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getSex(), is((String)null));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[0], is("足球"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[1], is(" 电影"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getFavors()[2], is("读书 "));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getMoney(), is(new BigDecimal("98235.01")));

		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOname(), is("obuildBeanFromRequest"));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOage(), is(770));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOages().length, is(3));
		assertThat("RequestUtil.buildBeanFromRequest()", person.getOtherPerson().getOages()[1], is(220));
	}
}
