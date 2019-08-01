package fd.ng.web.util;

import fd.ng.core.exception.BusinessProcessException;
import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.utils.JsonUtil;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultEnum;
import fd.ng.web.action.ActionResultHelper;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.helper.HttpDataHolder;
import fd.ng.web.helper.Loghelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {
	private static final Logger logger = LogManager.getLogger(ResponseUtil.class.getName());

	public static HttpServletResponse getResponse() {
		return HttpDataHolder.getResponse();
	}

	public static void writeJSON(HttpServletResponse response, ActionResult data) {
		response.setContentType("application/json; charset=utf-8");
		response.setCharacterEncoding("UTF-8"); // 防止中文乱码
		response.setStatus(HttpServletResponse.SC_OK);
		response.setHeader("Cache-Control","no-cache");
		response.setHeader("Cache-Control","no-store");
		response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
		response.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
		// 解决跨域访问的问题
		if(WebinfoConf.CORS_Allow) {
//			response.setHeader("Access-Control-Allow-Origin", WebinfoConf.CORS_acao);
			/**
			 * 配置了allow-credentials之后，如果allow-origin设为*，跨域时会报错说因为允许credentials，origin不能设为通配*
			 * 所以，可以随便设置某个domain？
			 * 前端 ajax 提交时，增加： xhrFields: { withCredentials: true }
			 */
			String oh = HttpDataHolder.getRequest().getHeader("Origin");
		    response.setHeader("Access-Control-Allow-Origin", oh);

			response.setHeader("Access-Control-Allow-Methods", WebinfoConf.CORS_acam);
//			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
			response.setHeader("Access-Control-Allow-Headers", "content-type, x-requested-with");
			response.setHeader("Access-Control-Allow-Credentials", WebinfoConf.CORS_acac); // 是否允许接收cookie信息
		}
		// 向响应中写入数据
		try {
			response.getWriter().println(JsonUtil.toJson(data));
		} catch (IOException e) {
			logger.error(Loghelper.fitMessage("Response write fail! data : " + data.toString()), e);
		}
		//PrintWriter writer = response.getWriter();
		//writer.write(JsonUtil.toJson(data));
		//writer.flush();
		//writer.close();
	}

	/**
	 * 把方法的返回值封装成标准JSON串（包括成功失败标志、错误码、提示信息、数据体）
	 * action处理成功，调用该方法。
	 * @param response
	 * @param data Action方法的返回值
	 */
	public static void writeActionResult(HttpServletResponse response, ActionResultEnum resultEnum, Object data) {
		ActionResult res;
		if(data==null) {
			res = new ActionResult(resultEnum.getCode(), resultEnum.getMessage());
		}
		else if( data instanceof ActionResult )
			res = (ActionResult)data;
		else
			res = new ActionResult(resultEnum.getCode(), resultEnum.getMessage(), data);
		writeJSON(response, res);
	}

	// 回写处理成功
	public static void writeActionSuccess(HttpServletResponse response) {
		writeActionSuccess(response, null);
	}
	public static void writeActionSuccess(HttpServletResponse response, Object data) {
		if(data==null) {
			writeJSON(response, ActionResultHelper.success());
			return;
		}
		if(data instanceof ActionResult)
			writeJSON(response, (ActionResult)data);
		else
			writeJSON(response, ActionResultHelper.success(data));
	}

	// 回写业务错误信息
	public static void writeActionBizError(HttpServletResponse response, String msgError) {
		writeJSON(response, ActionResultHelper.bizError(msgError));
	}
	public static void writeActionBizError(HttpServletResponse response, BusinessProcessException bex) {
		writeActionBizError(response, bex.getMessage());
	}
	public static void writeActionBizError(HttpServletResponse response, BusinessSystemException bex) {
		// 只把构造这个异常时使用的信息返回前端。被包裹的原始异常信息记录进入日志即可
		writeActionBizError(response, bex.getMineMessage());
	}
	public static void writeActionBizError(HttpServletResponse response, ActionResult ar) {
		writeJSON(response, ar);
	}

	// 回写系统错误信息
	public static void writeSystemError(HttpServletResponse response) {
		writeJSON(response, ActionResultHelper.systemError());
	}
	public static void writeSystemError(HttpServletResponse response, ActionResultEnum errorEnum) {
		writeJSON(response, ActionResultHelper.systemError(errorEnum));
	}
	public static void writeSystemError(HttpServletResponse response, String msgError) {
		writeJSON(response, ActionResultHelper.systemError(msgError));
	}
	public static void writeSystemError(HttpServletResponse response, Exception e) {
		writeSystemError(response, e.getMessage());
	}
	public static void writeSystemError(HttpServletResponse response, String msgError, Object data) {
		writeJSON(response, ActionResultHelper.systemError(msgError, data));
	}
}
