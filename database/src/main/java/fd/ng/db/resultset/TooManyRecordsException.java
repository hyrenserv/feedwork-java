package fd.ng.db.resultset;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;

public class TooManyRecordsException extends BaseInternalRuntimeException {

	private static final long serialVersionUID = -1295972292775341105L;

	public TooManyRecordsException() {}
	public TooManyRecordsException(final String msg) { super(msg);}
}
