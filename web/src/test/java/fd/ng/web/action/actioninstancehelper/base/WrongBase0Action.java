package fd.ng.web.action.actioninstancehelper.base;

import fd.ng.core.annotation.Param;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public  class WrongBase0Action extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(WrongBase0Action.class.getName());

	public static final int public_S_F_Field_WrongBase0 = -1;
	private static final int private_S_F_Field_WrongBase0 = -1;
	protected static final int protected_S_F_Field_WrongBase0 = -1;

	public String publicFieldWrongBase0;
	private String privateFieldWrongBase0;
	protected String protectedFieldWrongBase0;

	// 这个类只有这么一个public static方法，应该定义为抽象类
	public static void staticPublic() {

	}

	public void public0() {

	}

	@Param(name="i", desc = "", range = "")
	public void public0(int i) {

	}

}
