package fd.ng.web.fileupload.servlet;

import fd.ng.web.fileupload.FileUploadBase;
import fd.ng.web.fileupload.RequestContext;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Provides access to the request information needed for a request made to
 * an HTTP servlet.</p>
 *
 */
public class ServletRequestContext implements RequestContext {

    // ----------------------------------------------------- Instance Variables

    /**
     * The request for which the context is being provided.
     */
    private final HttpServletRequest request;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a context for this request.
     *
     * @param request The request to which this context applies.
     */
    public ServletRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Retrieve the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    @Override
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * 取得 request 的 content 长度. 默认取 Header 中的 Content-length ，没有再从 request 中取 getContentLength()
     *
     * @return request 的 content 长度
     */
    @Override
    public long contentLength() {
        long size;
        try {
            size = Long.parseLong(request.getHeader(FileUploadBase.CONTENT_LENGTH));
        } catch (NumberFormatException e) {
            size = request.getContentLength();
        }
        return size;
    }

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String toString() {
        return format("URI=%s, ContentLength=%s, ContentType=%s, getCharacterEncoding=%s",
                request.getRequestURI(), this.contentLength(), this.getContentType(), this.getCharacterEncoding());
    }

}
