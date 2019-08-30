package fd.ng.web.exception;

import fd.ng.core.exception.BusinessSystemException;

public class AppSystemException extends BusinessSystemException {
	public AppSystemException(final String msg) { super(msg);
	}
	public AppSystemException(final Throwable cause) {
		super(cause);
	}

	public AppSystemException(final String msg, Throwable cause) {
		super(msg, cause);
	}

	public AppSystemException(final String canbeGettedMessage, final String loggedMessage, Throwable cause) {
		super(canbeGettedMessage, loggedMessage, cause);
	}
}
