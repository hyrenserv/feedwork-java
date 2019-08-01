package fd.ng.web.exception;

import fd.ng.core.exception.BusinessSystemException;

public class AppSystemException extends BusinessSystemException {
	public AppSystemException(Throwable cause) {
		super(cause);
	}

	public AppSystemException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public AppSystemException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, loggedMessage, cause);
	}
}
