package fd.ng.web.action.actioninstancehelper.two;

import fd.ng.core.annotation.Param;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//@Action(UriExt= "/login")
public class Action1 extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(Action1.class.getName());

	public String welcome() {
		return "welcome 2";
	}

	@Param(name="username", alias = "uname", nullable = true, valueIfNull = {"1", "2"},
			desc = "desc 1", range = "1,2", ignore=true)
	@Param(name="password", alias = "pwd",
			desc = "desc p", range = "p:1,2")
	public String welcome1(String username, String password) {
		return "welcome " + username + " : " + password;
	}

	@Param(name="username", alias = "uname", nullable = true, valueIfNull = {"1", "2"},
			desc = "desc 1", range = "1,2", ignore=true)
	@Param(name="password", alias = "pwd",
			desc = "desc p", range = "p:1,2")
	public String welcome2(String username, String password) {
		return "welcome 2";
	}
}
