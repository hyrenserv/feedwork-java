package fd.ng.web.action.actioninstancehelper;

import fd.ng.web.action.actioninstancehelper.base.WrongBase0Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WrongBase1Action extends WrongBase0Action {
	private static final Logger logger = LogManager.getLogger(WrongBase1Action.class.getName());

	public static final int public_S_F_Field_WrongBase1 = -1;
	private static final int private_S_F_Field_WrongBase1 = -1;
	protected static final int protected_S_F_Field_WrongBase1 = -1;

	public String publicFieldWrongBase1;
	private String privateFieldWrongBase1;
	protected String protectedFieldWrongBase1;

	protected void nothingProtected() {

	}
	protected void nothingProtected(int i) {

	}
	private void nothingPrivate() {

	}
}
