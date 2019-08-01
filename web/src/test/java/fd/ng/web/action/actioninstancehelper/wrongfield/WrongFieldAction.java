package fd.ng.web.action.actioninstancehelper.wrongfield;

import fd.ng.web.action.actioninstancehelper.WrongBase1Action;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

public class WrongFieldAction extends WrongBase1Action {
	// 以下4个变量，用于验证Helper是否能够把他们找到
	private static final Logger logger = LogManager.getLogger(WrongFieldAction.class.getName());

	public static final int public_S_F_Field_Wrong = -1;
	private static final int private_S_F_Field_Wrong = -1;
	protected static final int protected_S_F_Field_Wrong = -1;

	public String publicFieldWrong;
	private String privateFieldWrong;
	protected String protectedFieldWrong;

	public String welcome(String username, String password) {
		return "welcome " + username + " : " + password;
	}
	public boolean signin(HttpServletRequest request,
	                      @RequestBean Person person) {
		return true;
	}
}
