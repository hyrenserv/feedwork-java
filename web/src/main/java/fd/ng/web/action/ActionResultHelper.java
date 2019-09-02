package fd.ng.web.action;

import fd.ng.core.exception.BusinessProcessException;
import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;

public class ActionResultHelper {
	public static ActionResult success() {
		return success(null);
	}
	public static ActionResult success(Object data) {
		return new ActionResult(ActionResultEnum.SUCCESS.getCode(), ActionResultEnum.SUCCESS.getMessage(), data);
	}

	public static ActionResult bizError(String errorMsg) {
		if(errorMsg==null) errorMsg = ActionResultEnum.BIZ_ERROR.getMessage();
		return new ActionResult(ActionResultEnum.BIZ_ERROR.getCode(), errorMsg);
	}
	public static ActionResult bizError(BusinessSystemException bex) {
		if(bex==null) {
			throw new FrameworkRuntimeException("Exception can not be null!");
		}

		// 只把构造这个异常时使用的信息返回前端。被包裹的原始异常信息记录进入日志即可
		String forFrontMsg = bex.getMineMessage(); // 获得用户构造这个异常时，明确指定的错误信息。
		if(forFrontMsg==null) forFrontMsg = ActionResultEnum.BIZ_ERROR.getMessage();
		forFrontMsg += " | " + bex.getErrorCode(); // 追加上错误代码。如果该异常被记入日志，可以用这个代码快速定位

		return new ActionResult(ActionResultEnum.BIZ_ERROR.getCode(), forFrontMsg);
	}
	public static ActionResult bizError(BusinessProcessException bex) {
		if(bex==null) {
			throw new FrameworkRuntimeException("Exception can not be null!");
		}
		if(bex.isNullCode()) return new ActionResult(ActionResultEnum.BIZ_ERROR.getCode(), bex.getMessage());
		else {
			if(bex.getCode()<1000)
				throw new FrameworkRuntimeException("You can only use code value more than 1000!");
			return new ActionResult(bex.getCode(), bex.getMessage());
		}
	}

	public static ActionResult systemError() {
		return systemError(null, null);
	}
	/**
	 * 构造系统内部错误的返回对象
	 * @param msgError 具体的错误信息。如果为空，则使用默认提示信息
	 * @param data 其他信息。比如，导致发生内部错误的数据，可以传递进来
	 * @return
	 */
	public static ActionResult systemError(String msgError, Object data) {
		if(StringUtil.isBlank(msgError)) msgError = ActionResultEnum.SYSTEM_ERROR.getMessage();
		return new ActionResult(ActionResultEnum.SYSTEM_ERROR.getCode(), msgError, data);
	}
	public static ActionResult systemError(String msgError) {
		return systemError(msgError, null);
	}
	public static ActionResult systemError(ActionResultEnum errorEnum) {
		return new ActionResult(errorEnum.getCode(), errorEnum.getMessage());
	}
	public static ActionResult systemError(Object data) {
		return systemError(null, data);
	}

	public static ActionResult fromJson(String json) {
		ActionResult ar = JsonUtil.toObject(json, ActionResult.class);
		return ar;
	}
}
