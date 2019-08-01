package fd.ng.web.hmfmswebapp.login;

import fd.ng.web.WebBaseTestCase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

// 按顺序执行，保证先完成登陆（获得session），再执行后续功能
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginActionUriFullPathSessionTest extends WebBaseTestCase {

	@Test
	public void t00_login() {
		String responseValue = post(
				getUrlActionPattern()+"/fd/ng/web/hmfmswebapp/loginfullpath/loginSession"
				, new String[][]{
				{"username", "admin"}, {"password", "admin"}
		});
		assertThat(responseValue, containsString("\"code\": 200,"));
	}

	@Test
	public void t10_checkSession() {
		String responseValue = post(
				getUrlActionPattern()+"/fd/ng/web/hmfmswebapp/loginfullpath/checkSession"
				, new String[][]{
						{"username", "admin"}, {"password", "admin"}
				});
		assertThat(responseValue, containsString("way"));
		assertThat(responseValue, containsString("session@user:admin"));
	}
}