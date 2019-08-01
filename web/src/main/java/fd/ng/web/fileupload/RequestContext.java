package fd.ng.web.fileupload;

import java.io.InputStream;
import java.io.IOException;

/**
 * <p>Abstracts access to the request information needed for file uploads. This
 * interface should be implemented for each type of request that may be
 * handled by FileUpload, such as servlets and portlets.</p>
 *
 */
public interface RequestContext {

    /**
     * Retrieve the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    String getCharacterEncoding();

    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    String getContentType();

//    /**
//     * Retrieve the content length of the request.
//     *
//     * @return The content length of the request.
//     * @deprecated 1.3 Use {@link UploadContext#contentLength()} instead
//     */
//    @Deprecated
//    int getContentLength();

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @since 1.3
     */
    long contentLength();

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    InputStream getInputStream() throws IOException;

}
