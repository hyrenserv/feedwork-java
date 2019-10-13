package fd.ng.web.action.actioninstancehelper.two;

import fd.ng.core.annotation.Param;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//@Action(UriExt= "/login")
public class Action2 extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(Action2.class.getName());

	public String welcome2() {
		return "welcome 2";
	}

	@Param(name="username", desc = "", range = "")
	@Param(name="password", isBean = true, desc = "", range = "")
	public String welcome(String username, String password) {
		return "welcome " + username + " : " + password;
	}

}
