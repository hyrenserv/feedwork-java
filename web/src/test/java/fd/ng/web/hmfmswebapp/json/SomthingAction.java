package fd.ng.web.hmfmswebapp.json;

import com.google.gson.reflect.TypeToken;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import fd.ng.web.annotation.Action;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于测试前端提交json的情况
 */
public class SomthingAction extends WebappBaseAction {
	public String welcome() {
		String json = RequestUtil.getJson();
		return json;
	}

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
			RequestUtil.putSessValue("id", id);
			RequestUtil.putSessValue("val-123", 123);
			RequestUtil.putSessValue("val-123-str", "123");
			RequestUtil.putSessValue("val-float-123.01", Float.parseFloat("123.01"));
			RequestUtil.putSessValue("val-double-123.02", Double.parseDouble("123.02"));
			RequestUtil.putSessValue("val-123.03", 123.03);
			RequestUtil.putSessValue("val-123.00", 123.00);
			RequestUtil.putSessValue("val-string", "xxx");

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	public Map<String, Object> checkSession() {
		Integer intNull = RequestUtil.getSessValue("nullnullnull"); // 取一个不存在的值对象

		Map<String, String> userSess = RequestUtil.getSessValue("user");
		if(userSess==null) {
			System.out.println("No Session Data");
			Map<String, Object> result = new HashMap<>();
			result.put("bizMsg", "No Session Data");
			return result;
		}

		long id = RequestUtil.getSessValue("id");
		System.out.println("user : " + (userSess==null?"null":Arrays.toString(userSess.values().toArray())));
		System.out.println("id : " + id);
		Map<String, Object> result = new HashMap<>();
		result.put("user", userSess);
		result.put("id", id);
		result.put("intNull", "intNull="+intNull);
		return result;
	}

	public Map<String, Object> checkSessionTwo() {
		Map<String, String> userSess = RequestUtil.getSessValue("user");
		long id = RequestUtil.getSessValue("id");
		System.out.println("user : " + (userSess==null?"null":Arrays.toString(userSess.values().toArray())));
		System.out.println("id : " + id);
		Map<String, Object> result = new HashMap<>();
		result.put("user", userSess);
		result.put("id", id);
		return result;
	}

	// --------- Cookie ------------

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
			RequestUtil.putCookieObject("cokiId", id);

			return ActionResultHelper.success();
		}
		else
			return ActionResultHelper.bizError("login failed!");
	}

	public Map<String, Object> checkCookie() {
		Map<String, String> userSess = RequestUtil.getCookieObject(
				"cokiUser", new TypeToken<HashMap<String, String>>(){}.getType());
		if(userSess==null) {
			System.out.println("No Cookie Data");
			Map<String, Object> result = new HashMap<>();
			result.put("bizMsg", "No Cookie Data");
			return result;
		}
		long id = RequestUtil.getCookieObject("cokiId", long.class);
		System.out.println("cokiUser : " + (userSess==null?"null":Arrays.toString(userSess.values().toArray())));
		System.out.println("cokiId : " + id);
		Map<String, Object> result = new HashMap<>();
		result.put("cokiUser", userSess);
		result.put("cokiId", id);
		return result;
	}

	public Map<String, Object> checkCookieTwo() {
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
}
