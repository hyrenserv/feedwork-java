package fd.ng.web.hmfmswebapp.customs;

import fd.ng.core.conf.AppinfoConf;
import fd.ng.web.WebBaseTestCase;
import fd.ng.web.conf.WebinfoConf;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class CustomsTest extends WebBaseTestCase {
	@Test
	public void testMyServlet() {
		String responseValue = post(
				getUrlCtx() + "/my/anyuri"
				, new String[][]{
						{"name", "toMyServlet"}
				});
		assertThat(responseValue, containsString("name=toMyServlet"));
	}

	@Test
	public void testMyFilter() {
		String responseValue = post(
				getUrlCtx() + "/my/anyuri"
				, new String[][]{
						{"name", "toMyServlet"}
				});
		assertThat(responseValue, containsString("name=toMyServlet"));
		assertThat(responseValue, containsString("newValue=MyFilter, by MyFilter"));
	}

	@Test
	public void testWrapFdwebFilter() {
		// WrapFdwebFilter 是针对 webservlet 包裹，所以任意访问任何一个Action都应该得到预期数据
		String responseValue = post(
				getUrlActionPattern() + "/" + AppinfoConf.AppBasePackage.replace(".", "/") + "/a0101/welcome"
				, new String[][]{
						{"name", "toA0101Welcome"}
				});
		assertThat(responseValue, containsString("toA0101Welcome"));
		assertThat(responseValue, containsString("WrapFdwebFilter in welcome"));
	}
}
