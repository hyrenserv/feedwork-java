package fd.ng.web.fileupload;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;

public class FileUploadException extends BaseInternalRuntimeException {
    private static final long serialVersionUID = 7088932836768787907L;

    public FileUploadException(String msg) {
        super(msg);
    }

    public FileUploadException(Throwable cause) {
        super(cause);
    }

    public FileUploadException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public FileUploadException(String canbeGettedMessage, String loggedMessage) {
        super(canbeGettedMessage, loggedMessage);
    }

    public FileUploadException(String canbeGettedMessage, String loggedMessage, Throwable cause) {
        super(canbeGettedMessage, loggedMessage, cause);
    }
}
