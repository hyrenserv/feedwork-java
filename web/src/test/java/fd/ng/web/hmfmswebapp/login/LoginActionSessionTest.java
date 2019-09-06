package fd.ng.web.hmfmswebapp.login;

import fd.ng.web.WebBaseTestCase;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

// session 和 cookie 的测试用例要分开写，否则会出问题？？
// 按顺序执行，保证先完成登陆（获得session），再执行后续功能
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginActionSessionTest extends WebBaseTestCase {

	@Test
	public void t00_loginAtSession() {
		String responseValue = post(
				getActionUrl("dologin/loginAtSession")
				, new String[][]{
				{"username", "admin"}, {"password", "admin"}
		});
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":200,"));
	}

	@Test
	public void t10_checkSession() {
		String responseValue = post(getActionUrl("dologin/checkSession"));
		assertThat(responseValue, containsString("way"));
		assertThat(responseValue, containsString("session@user:admin"));
		assertThat(responseValue, containsString("intNull=null"));
	}

	@Test
	public void t11_checkSessionTwo() {
		String responseValue = post(getActionUrl("dologin/checkSessionTwo"));
		assertThat(responseValue, containsString("way"));
		assertThat(responseValue, containsString("session@user:admin"));
	}

//	@Test
//	public void t00_loginAtCookie() {
//		String ar = post(
//				getActionUrl("dologin/loginAtCookie")
//				, new String[][]{
//						{"username", "admin"}, {"password", "admin"}
//				});
//	}
//
//	@Test
//	public void t20_checkCookie() {
//		String ar = post(getActionUrl("dologin/checkCookie"));
//		assertThat(ar, containsString("way"));
//		assertThat(ar, containsString("cookie@user:admin"));
//	}
}