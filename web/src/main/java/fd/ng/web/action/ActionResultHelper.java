package fd.ng.web.action;

import fd.ng.core.exception.BusinessProcessException;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;

public class ActionResultHelper {
	public static ActionResult success() {
		return success(null);
	}
	public static ActionResult success(Object data) {
		return new ActionResult(ActionResultEnum.SUCCESS.getCode(), ActionResultEnum.SUCCESS.getMessage(), data);
	}

	/**
	 * 业务层抛出BusinessException时，构造的ActionResult
	 * @param data
	 * @return
	 */
	public static ActionResult bizError(BusinessProcessException bex, Object data) {
		if(bex==null) {
			throw new FrameworkRuntimeException("BusinessException can not be null!");
		}
		return new ActionResult(ActionResultEnum.BIZ_ERROR.getCode(), bex.getMessage(), data);
	}
	public static ActionResult bizError(String errorMsg) {
		if(errorMsg==null) errorMsg = ActionResultEnum.BIZ_ERROR.getMessage();
		return new ActionResult(ActionResultEnum.BIZ_ERROR.getCode(), errorMsg);
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
