package fd.ng.web.action.actioninstancehelper.three;

import fd.ng.web.annotation.RequestBean;
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

	public boolean signin(HttpServletRequest request,
	                      @RequestBean Person person) {
		return true;
	}
}
