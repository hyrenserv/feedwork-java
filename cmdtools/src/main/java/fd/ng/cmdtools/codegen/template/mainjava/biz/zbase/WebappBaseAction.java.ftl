package ${basePackage}.${subPackage};

import fd.ng.web.action.AbstractWebappBaseAction;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import fd.ng.web.util.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 这是一个样板代码。
 * 每个项目中，要编写这个类（必须继承 AbstractWebappBaseAction ），作为所有Action的父类！！！
 * 大多数情况下，重载 _doPreProcess 方法，实现进行 session 验证的处理逻辑即可。
 */
public abstract class ${className} extends AbstractWebappBaseAction {
	private static final Logger logger = LogManager.getLogger(${className}.class.getName());

	@Override
	protected ActionResult _doPreProcess(HttpServletRequest request) {
		// session 检查。
		Map<String, String> loginUser = RequestUtil.getSessValue("user");
		if(loginUser==null)
			return ActionResultHelper.bizError("please login first!");

		return null; // 验证通过
	}
}
