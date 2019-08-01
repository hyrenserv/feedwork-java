package fd.ng.web.handler;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;
import fd.ng.core.exception.BusinessProcessException;
import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.UuidUtil;
import fd.ng.web.action.AbstractBaseAction;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultEnum;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.helper.ActionInstanceHelper;
import fd.ng.web.helper.ParamsHelper;
import fd.ng.web.helper.Loghelper;
import fd.ng.web.util.ResponseUtil;
import fd.ng.web.helper.HttpDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class WebServlet extends HttpServlet {
	protected static final Logger logger = LogManager.getLogger(WebServlet.class.getName());
	private static final long serialVersionUID = -5910553791542084347L;

	public WebServlet() {  }

	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response) {
//		showRequestInfo(request, response);
		String pathInfo = request.getPathInfo(); // ctx 后面的整个URI
		if(pathInfo.startsWith(ActionInstanceHelper.HEAD_URI)) {
			int lastLoc = pathInfo.lastIndexOf('/');
			if(lastLoc<2){ // 因为pathInfo是以'/'开头的，所以，最后一个'/'的位置至少应该从2开始。这是极限情况，uri类似：/a/b
				logger.warn("Invalid url! ContextPath={}, uri={}, queryString : {}", request.getContextPath() , pathInfo, request.getQueryString());
				return;
			}
			long startTime = 0L;
			if(logger.isDebugEnabled() || WebinfoConf.ActionLongtime>0) // 记录当前线程 ID ，访问的 url
				startTime = System.currentTimeMillis();
			try {
				HttpDataHolder.init(request, response);
				if(logger.isDebugEnabled()) { // 记录当前线程 ID ，访问的 url
					logger.debug(Loghelper.fitMessage("START. url=%s", request.getRequestURL()));
				}
				// 获取 Action 对象
				String actionPathName = HttpDataHolder.getActionPathName();
				Object actionClassObject = ActionInstanceHelper.getAction(actionPathName);
				if(actionClassObject==null) throw new FrameworkRuntimeException("Can not found Action object by uri["+pathInfo+"]");
				// 获取 Action 方法
				String actionMethodName = HttpDataHolder.getActionMethodName();
				Method actionMethod = ActionInstanceHelper.getActionMethod(actionPathName, actionMethodName);
				if(actionMethod==null) throw new FrameworkRuntimeException("Can not found Action method["+actionMethodName+"] by uri["+pathInfo+"]");
				Method preProcessMethod = ActionInstanceHelper.getActionMethod(actionPathName, AbstractBaseAction.PreProcess_MethodName);
				Method postProcessMethod = ActionInstanceHelper.getActionMethod(actionPathName, AbstractBaseAction.PostProcess_MethodName);

				// 开始业务处理
				if(!doPrePostProcess(actionClassObject, preProcessMethod, request, response)) return;
				actionMethod.setAccessible(true);
				Object[] args = ParamsHelper.autowireParameters(request, actionMethod);
				Object result = actionMethod.invoke(actionClassObject, args);
				if(!doPrePostProcess(actionClassObject, postProcessMethod, request, response)) return;
				ResponseUtil.writeActionSuccess(response, result);
			} catch (InvocationTargetException e) { // Methos.invoke 方式执行的方法内部产生的异常
				doExceptionProcess(request, response);
				Throwable t = e.getTargetException();// 获取目标异常
				if( t instanceof BusinessProcessException) {
					// logger.debug(e);
					ResponseUtil.writeActionBizError(response, (BusinessProcessException)t);
				} else if (t instanceof BusinessSystemException) {
					ResponseUtil.writeActionBizError(response, (BusinessSystemException)t);
				} else if (t instanceof BaseInternalRuntimeException) {
					dealFrameworkInternalException(request, response, t);
				} else {
					// Action方法内部发生了未知的异常
					writeUnknownedSystemException(request, response, t);
				}
			} catch (Exception e) {
				// 理论上，这里不需要执行doExceptionProcess，因为异常走到这里，肯定不是Action中的方法产生的。
				logger.error("Other Exception : " + e.getMessage());
				doExceptionProcess(request, response);
				if (e instanceof BaseInternalRuntimeException)
					dealFrameworkInternalException(request, response, e);
				else
					writeUnknownedSystemException(request, response, e);
			} finally {
				String bizid = HttpDataHolder.getBizId(); // 因为下面的release会释放资源，所以只好提前取值
				HttpDataHolder.relase();
				if(WebinfoConf.ActionLongtime>0) {
					long et = System.currentTimeMillis() - startTime;
					if(et> WebinfoConf.ActionLongtime)
						logger.warn("{} END. Processing time : {}ms, url={}", bizid, et, request.getRequestURL());
					else if(logger.isDebugEnabled())
						logger.debug("{} END. Processing time : {}ms, url={}", bizid, et, request.getRequestURL());
				} else if(logger.isDebugEnabled()) {
					long et = System.currentTimeMillis() - startTime;
					logger.debug("{} END. Processing time : {}ms, url={}", bizid, et, request.getRequestURL());
				}
			}
		}
		else {
			logger.warn(getRequestInfo(request));
			ResponseUtil.writeSystemError(response, ActionResultEnum.NOTFOUND_ERROR);
		}
	}

	/**
	 * 仅仅用于 Action 的前处理和后处理
	 * @param actionClassObject
	 * @param method
	 * @param request
	 * @param response
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private boolean doPrePostProcess(Object actionClassObject, Method method,
	                          HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {
		// TODO 这里可以对ActionFilter注解进行处理。该注解可以定义执行位置：beforeInternalProcess / afterInternalProcess，以便在预期位置执行
//		Method _doProcess = ActionInstanceHelper.getActionMethod(actionPackageName, methodName);
		if(method==null) // 没有定义前/后处理，则method对象为null。比如处理用户登陆的Action，不需要继承基类，则没有前后处理
			return true;
		method.setAccessible(true);
		ActionResult result = (ActionResult)method.invoke(actionClassObject, request);
		if( result!=null && !result.isSuccess() ) {
			// 前置或后置函数，返回了值，并且其代码不是成功，因此，回写业务处理错误
			ResponseUtil.writeActionBizError(response, result);
			return false;
		}
		return true;
	}

	private void doExceptionProcess(HttpServletRequest request, HttpServletResponse response) {
		try{ // 处理异常
			String actionPathName = HttpDataHolder.getActionPathName();
			Object actionClassObject = ActionInstanceHelper.getAction(actionPathName);
			Method exProcessMethod = ActionInstanceHelper.getActionMethod(actionPathName, AbstractBaseAction.ExceptionProcess_MethodName);
			if(exProcessMethod==null) {
				logger.warn(Loghelper.fitMessage("Not provided doExceptionProcess() method"));
				return;
			}
			exProcessMethod.setAccessible(true);
			exProcessMethod.invoke(actionClassObject, request);
		} catch (Exception ex) {
			logger.error(Loghelper.fitMessage("_doExceptionProcess() invoke failed"), ex);
		}
	}

	private void dealFrameworkInternalException(HttpServletRequest request, HttpServletResponse response, Throwable t) {
		if (t instanceof FrameworkRuntimeException) {
			FrameworkRuntimeException ex = (FrameworkRuntimeException)t;
			String errCode = BaseInternalRuntimeException.ERRCODE_LOGPREFIX + ex.getErrorCode();
			//String errMsgAndCode = (t.getMessage()==null?"Framework Internal Error.":t.getMessage()) + errCode;
			String errMsgAndCode = "Framework Internal Error." + errCode;
			// 因为在构造异常的时候已经 log 了异常堆栈，所以，这里仅打印 URI 和错误码，不需要打印异常堆栈
			logger.error(Loghelper.fitMessage("url=%s %s", request.getRequestURI(), errCode));
			ResponseUtil.writeSystemError(response, errMsgAndCode);
		} else if (t instanceof RawlayerRuntimeException) {
			RawlayerRuntimeException ex = (RawlayerRuntimeException)t;
			String errCode = BaseInternalRuntimeException.ERRCODE_LOGPREFIX + ex.getErrorCode();
			//String errMsgAndCode = (t.getMessage()==null?"Framework Internal Error.":t.getMessage()) + errCode;
			String errMsgAndCode = "Rawlayer Internal Error." + errCode;
			// 因为在构造异常的时候已经 log 了异常堆栈，所以，这里仅打印 URI 和错误码，不需要打印异常堆栈
			logger.error(Loghelper.fitMessage("url=%s %s", request.getRequestURI(), errCode));
			ResponseUtil.writeSystemError(response, errMsgAndCode);
		} else { // 绝对不应该走到这个分支
			logger.fatal(Loghelper.fitMessage("programe error! URL="+request.getRequestURI()), t);
			writeUnknownedSystemException(request, response, t);
		}
	}

	private void writeUnknownedSystemException(HttpServletRequest request, HttpServletResponse response, Throwable t) {
		String errCode = BaseInternalRuntimeException.ERRCODE_LOGPREFIX + UuidUtil.uuid();
		//String errMsgAndCode = (t.getMessage()==null?"System Internal Error.":t.getMessage()) + errCode;
		logger.error(Loghelper.fitMessage(request.getRequestURI() + errCode), t);

		ResponseUtil.writeSystemError(response, "System Internal Error." + errCode);
	}

	private String getRequestInfo(HttpServletRequest request) {
//		if(request!=null) {
//			String str = "\n";
//			str += String.format(" | ContextPath : %s%n", request.getContextPath());
//			str += String.format(" | RequestURI  : %s%n", request.getRequestURI());
//			str += String.format(" | PathInfo    : %s%n", request.getPathInfo());
//			str += String.format(" | RequestURL  : %s%n", request.getRequestURL());
//			str += String.format(" | ServletPath : %s%n", request.getServletPath());
//			str += String.format(" | QueryString : %s%n", request.getQueryString());
//		}
		StringBuilder sb = new StringBuilder();
		Map<String, String[]> paramsMap = request.getParameterMap();
		paramsMap.forEach((key, value)->{
			sb.append(key).append('=').append(Arrays.toString(value)).append("  ");
		});
		return sb.toString();
	}
}
