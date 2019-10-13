package fd.ng.web.action.actioninstancehelper.three;

import fd.ng.core.annotation.Param;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

//@Action(UriExt= "/login")
public class Action3 extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(Action3.class.getName());

	public String welcome3() {
		return "welcome 3";
	}

	@Param(name="request", desc = "", range = "")
	@Param(name="person", isBean = true, desc = "", range = "")
	public boolean signin(HttpServletRequest request,
	                      Person person) {
		return true;
	}
}
