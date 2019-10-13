package fd.ng.web.action.actioninstancehelper.uri;

import fd.ng.core.annotation.Param;
import fd.ng.web.action.ActionResult;
import fd.ng.web.annotation.Action;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Action(UriExt = "^/login")
public class UriAction3 extends WebappBaseAction {
	// 以下4个变量，用于验证Helper是否能够把他们找到
	private static final Logger logger = LogManager.getLogger(UriAction3.class.getName());
	public String publicFieldUri;
	private String privateFieldUri;
	protected String protectedFieldUri;

	@Param(name="username", desc = "", range = "")
	@Param(name="password", desc = "", range = "")
	public ActionResult login(String username, String password) {
		return new ActionResult();
	}

	@Param(name="username", desc = "", range = "")
	@Param(name="password", desc = "", range = "")
	@Param(name="person", isBean = true, desc = "", range = "")
	public boolean signin(String username, String password,
	                      Person person) {
		return true;
	}
}
