/**
 *    <p>
 *      A disk-based implementation of the
 *      {@link fd.ng.web.fileupload.FileItem FileItem}
 *      interface. This implementation retains smaller items in memory, while
 *      writing larger ones to disk. The threshold between these two is
 *      configurable, as is the location of files that are written to disk.
 *    </p>
 *    <p>
 *      In typical usage, an instance of
 *      {@link fd.ng.web.fileupload.disk.DiskFileItemFactory DiskFileItemFactory}
 *      would be created, configured, and then passed to a
 *      {@link fd.ng.web.fileupload.FileUpload FileUpload}
 *      implementation such as
 *      {@link fd.ng.web.fileupload.servlet.ServletFileUpload ServletFileUpload}
 *    </p>
 *    <p>
 *      The following code fragment demonstrates this usage.
 *    </p>
 * <pre>
 *        DiskFileItemFactory factory = new DiskFileItemFactory();
 *        // maximum size that will be stored in memory
 *        factory.setSizeThreshold(4096);
 *        // the location for saving data that is larger than getSizeThreshold()
 *        factory.setRepository(new File("/tmp"));
 *
 *        ServletFileUpload upload = new ServletFileUpload(factory);
 * </pre>
 */
package fd.ng.web.fileupload.disk;
