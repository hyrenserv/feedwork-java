package fd.ng.web.action;

import fd.ng.web.annotation.Action;

import javax.servlet.http.HttpServletRequest;

@Action
public abstract class AbstractBaseAction {
	public static final String PreProcess_MethodName = "_doPreProcess";
	public static final String PostProcess_MethodName = "_doPostProcess";
	public static final String ExceptionProcess_MethodName = "_doExceptionProcess";
	/**
	 * 重载本方法，用于处理ACTION类中的每个方法在执行前的处理逻辑。比如Session验证。
	 * 处理成功：返回null，继续执行ACTION的方法；
	 * 处理失败：返回 Obejct：
	 * @param request
	 * @return
	 */
	protected abstract ActionResult _doPreProcess(HttpServletRequest request);

	/**
	 * 重载本方法，用于处理ACTION类中的每个方法在执行完成后的处理逻辑。比如提交事务、释放数据库连接。
	 * 处理成功：返回null，整个流程结束；
	 * 处理失败：返回 Obejct：
	 * @param request
	 * @return
	 */
	protected abstract ActionResult _doPostProcess(HttpServletRequest request);

	/**
	 * 重载本方法，用于处理ACTION类中的每个方法在执行中发生了未捕获的异常时的处理逻辑。比如回滚数据库事务。
	 * 在ACTION的各方法中主动抛出了异常，或者发生了系统不受检异常时，都会出发本函数
	 * 本方法不需要任何返回值
	 * 处理失败：返回 Obejct：
	 * @param request
	 * @return
	 */
	protected abstract void _doExceptionProcess(HttpServletRequest request);
}
