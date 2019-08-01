package fd.ng.web.hmfmswebapp.login;

import com.google.gson.reflect.TypeToken;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import fd.ng.web.annotation.Action;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Action(UriExt= "^/fd/ng/web/hmfmswebapp/loginfullpath")
public class LoginActionUriFullPath extends WebappBaseAction {
	public ActionResult loginSession(String username, String password) {
		if("admin".equalsIgnoreCase(username)&&"admin".equalsIgnoreCase(password)) {
			long id = System.currentTimeMillis();
			Map<String, Object> userSess = new HashMap<>();
			userSess.put("username", "admin");
			userSess.put("way", "session@user:admin");
			userSess.put("type", "root");
			userSess.put("id", id);
			userSess.put("isAdmin", true);

			RequestUtil.putSessValue("user", userSess);
			RequestUtil.putSessValue("id", id);

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	public ActionResult loginCookie(String username, String password) {
		if("admin".equalsIgnoreCase(username)&&"admin".equalsIgnoreCase(password)) {
			long id = System.currentTimeMillis();
			Map<String, Object> userSess = new HashMap<>();
			userSess.put("username", "admin");
			userSess.put("way", "cookie@user:admin");
			userSess.put("type", "root");
			userSess.put("id", id);
			userSess.put("isAdmin", true);

			RequestUtil.putCookieObject("cokiUser", userSess);
			RequestUtil.putCookieObject("cokiId", id);

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	public Map<String, Object> checkSession() {
		Map<String, String> userSess = RequestUtil.getSessValue("user");
		long id = RequestUtil.getSessValue("id");
		System.out.println("user : " + Arrays.toString(userSess.values().toArray()));
		System.out.println("id : " + id);
		Map<String, Object> result = new HashMap<>();
		result.put("user", userSess);
		result.put("id", id);
		return result;
	}

	public Map<String, Object> checkCookie() {
		Map<String, String> userSess = RequestUtil.getCookieObject(
				"cokiUser", new TypeToken<HashMap<String, String>>(){}.getType());
		long id = RequestUtil.getCookieObject("cokiId", long.class);
		System.out.println("cokiUser : " + (userSess==null?"null":Arrays.toString(userSess.values().toArray())));
		System.out.println("cokiId : " + id);
		Map<String, Object> result = new HashMap<>();
		result.put("cokiUser", userSess);
		result.put("cokiId", id);
		return result;
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
