package fd.ng.web.fileupload.util;

import java.io.IOException;

/**
 * Interface of an object, which may be closed.
 */
public interface Closeable {

    /**
     * Closes the object.
     *
     * @throws IOException An I/O error occurred.
     */
    void close() throws IOException;

    /**
     * Returns, whether the object is already closed.
     *
     * @return True, if the object is closed, otherwise false.
     * @throws IOException An I/O error occurred.
     */
    boolean isClosed() throws IOException;

}
