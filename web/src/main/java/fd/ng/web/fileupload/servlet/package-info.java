/**
 *    <p>
 *      An implementation of
 *      {@link fd.ng.web.fileupload.FileUpload FileUpload}
 *      for use in servlets conforming to JSR 53. This implementation requires
 *      only access to the servlet's current <code>HttpServletRequest</code>
 *      instance, and a suitable
 *      {@link fd.ng.web.fileupload.FileItemFactory FileItemFactory}
 *      implementation, such as
 *      {@link fd.ng.web.fileupload.disk.DiskFileItemFactory DiskFileItemFactory}.
 *    </p>
 *    <p>
 *      The following code fragment demonstrates typical usage.
 *    </p>
 * <pre>
 *        DiskFileItemFactory factory = new DiskFileItemFactory();
 *        // Configure the factory here, if desired.
 *        ServletFileUpload upload = new ServletFileUpload(factory);
 *        // Configure the uploader here, if desired.
 *        List fileItems = upload.parseRequest(request);
 * </pre>
 */
package fd.ng.web.fileupload.servlet;
