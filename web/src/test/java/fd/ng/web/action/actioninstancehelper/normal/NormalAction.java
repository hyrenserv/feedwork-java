package fd.ng.web.action.actioninstancehelper.normal;

import fd.ng.core.annotation.Param;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

// 本包下有3个Action，用于验证同一包下不能有超过2个Action
//@Action(UriExt= "/login")
public class NormalAction extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(NormalAction.class.getName());

	public static String staticMethod1() {
		return "staticMethod1 ";
	}

	public String welcome1() {
		return "welcome ";
	}

	@Param(name="username", desc = "", range = "")
	@Param(name="password", desc = "", range = "")
	public String welcome1(String username, String password) {
		return "welcome " + username + " : " + password;
	}

	// 和 Action3 中方法重名，用于验证：同一个包下跨Action不能有重名方法
	@Param(name="request", desc = "", range = "")
	@Param(name="person", isBean = true, desc = "", range = "")
	public boolean signin(HttpServletRequest request,
	                      Person person) {
		return true;
	}
}
