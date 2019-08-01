package fd.ng.web.hmfmswebapp.session;

import fd.ng.web.action.ActionResult;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

public class SessionAction extends WebappBaseAction {
	private static final String WebApp_Base = "/fd/ng/web/hmfmswebapp";

	public void testGetValue() {
		Map<String, String> userSess = RequestUtil.getSessValue("user");
		long id = RequestUtil.getSessValue("id");
		System.out.println("user : " + Arrays.toString(userSess.values().toArray()));
		System.out.println("id : " + id);
	}

	/**
	 * 屏蔽基类中对 session 的检查。因为登陆验证时，还没有 session。
	 * @param request
	 * @return
	 */
	@Override
	protected ActionResult _doPreProcess(HttpServletRequest request) {
		return null;
	}
}
