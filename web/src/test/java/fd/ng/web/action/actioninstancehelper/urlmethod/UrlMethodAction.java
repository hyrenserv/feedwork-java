package fd.ng.web.action.actioninstancehelper.urlmethod;

import fd.ng.core.annotation.Param;
import fd.ng.web.action.ActionResult;
import fd.ng.web.annotation.UrlName;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.hmfmswebapp.a0101.Person;

public class UrlMethodAction extends WebappBaseAction {

//	public ActionResult getSomething(String username, String password) {
//		return new ActionResult();
//	}

	@Param(name="username", desc = "", range = "")
	@Param(name="password", desc = "", range = "")
	@Param(name="person", isBean = true, desc = "", range = "")
	public ActionResult getSomething(String username, String password,
	                      Person person) {
		return null;
	}
//	@UrlName("getSomething")
//	public ActionResult getOne(String username, String password,
//	                                 @RequestBean Person person) {
//		return null;
//	}
	@UrlName("getSomething1")
	@Param(name="username", desc = "", range = "")
	@Param(name="password", desc = "", range = "")
	@Param(name="age", desc = "", range = "")
	public ActionResult getSomething(String username, String password, int age) {
		return null;
	}

//	public ActionResult addAnthing(String username, String password) {
//		return new ActionResult();
//	}
//
//	public ActionResult addAnthing(String username, String password,
//	                                 @RequestBean Person person) {
//		return null;
//	}
}
