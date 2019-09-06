package fd.ng.web.hmfmswebapp.login;

import fd.ng.web.WebBaseTestCase;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

// 按顺序执行，保证先完成登陆（获得cookie），再执行后续功能
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginActionCookieTest extends WebBaseTestCase {

	@Test
	public void t00_loginAtCookie() {
		String responseValue = post(
				getActionUrl("dologin/loginAtCookie")
				, new String[][]{
						{"username", "admin"}, {"password", "admin"}
				});
		assertThat(responseValue.replace(": ", ":"), containsString("\"code\":200,"));
	}

	@Test
	public void t20_checkCookie() {
		String responseValue = post(getActionUrl("dologin/checkCookie"));
		assertThat(responseValue, containsString("way"));
		assertThat(responseValue, containsString("cookie@user:admin"));
	}
	@Test
	public void t21_checkCookieTwo() {
		String responseValue = post(getActionUrl("dologin/checkCookieTwo"));
		assertThat(responseValue, containsString("way"));
		assertThat(responseValue, containsString("cookie@user:admin"));
	}
}