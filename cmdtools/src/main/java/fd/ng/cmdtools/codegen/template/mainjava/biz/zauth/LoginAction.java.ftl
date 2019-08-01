package ${basePackage}.${subPackage};

import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import fd.ng.web.util.RequestUtil;
import ${basePackage}.biz.zbase.WebappBaseAction;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

//@Action(UriExt= "dologin")
public class LoginAction extends WebappBaseAction {
	public ActionResult loginAtSession(String username, String password) {
		if("admin".equalsIgnoreCase(username)&&"admin".equalsIgnoreCase(password)) {
			long id = System.currentTimeMillis();
			Map<String, Object> userSess = new HashMap<>();
			userSess.put("username", "admin");
			userSess.put("way", "session@user:admin");
			userSess.put("type", "root");
			userSess.put("id", id);
			userSess.put("isAdmin", true);

			RequestUtil.putSessValue("user", userSess);

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	public ActionResult loginAtCookie(String username, String password) {
		if("admin".equalsIgnoreCase(username)&&"admin".equalsIgnoreCase(password)) {
			long id = System.currentTimeMillis();
			Map<String, Object> userSess = new HashMap<>();
			userSess.put("username", "admin");
			userSess.put("way", "cookie@user:admin");
			userSess.put("type", "root");
			userSess.put("id", id);
			userSess.put("isAdmin", true);

			RequestUtil.putCookieObject("cokiUser", userSess);

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	/**
	 * 屏蔽基类中对 session 的检查。因为登陆验证时，还没有 session。
	 */
	@Override
	protected ActionResult _doPreProcess(HttpServletRequest request) {
		return null;
	}
}
