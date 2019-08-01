package fd.ng.db;

import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.StringUtil;

public class DBException extends RawlayerRuntimeException {
	private static final long serialVersionUID = 2666035379025678137L;

	public DBException(final String id, final String msg) {
		super(id + StringUtil.BLANK + msg);
	}
	public DBException(final String id, final Throwable cause) {
		super(id, cause);
	}
	public DBException(final String id, final String msg, final Throwable cause) {
		super(id + StringUtil.BLANK + msg, cause);
	}
}
