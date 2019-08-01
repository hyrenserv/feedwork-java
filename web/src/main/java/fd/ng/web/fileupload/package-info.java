/**
 * <p>
 * A component for handling HTML file uploads as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt" target="_top">RFC&nbsp;1867</a>.
 * This component provides support for uploads within both servlets (JSR 53)
 * and portlets (JSR 168).
 * </p>
 * <p>
 * While this package provides the generic functionality for file uploads,
 * these classes are not typically used directly. Instead, normal usage
 * involves one of the provided extensions of
 * {@link fd.ng.web.fileupload.FileUpload FileUpload} such as
 * {@link fd.ng.web.fileupload.servlet.ServletFileUpload ServletFileUpload}
 * with a factory for
 * {@link fd.ng.web.fileupload.FileItem FileItem} instances,
 * such as
 * {@link fd.ng.web.fileupload.disk.DiskFileItemFactory DiskFileItemFactory}.
 * </p>
 * <p>
 * The following is a brief example of typical usage in a servlet, storing
 * the uploaded files on disk.
 * </p>
 * <pre>public void doPost(HttpServletRequest req, HttpServletResponse res) {
 *   DiskFileItemFactory factory = new DiskFileItemFactory();
 *   // maximum size that will be stored in memory
 *   factory.setSizeThreshold(4096);
 *   // the location for saving data that is larger than getSizeThreshold()
 *   factory.setRepository(new File("/tmp"));
 *
 *   ServletFileUpload upload = new ServletFileUpload(factory);
 *   // maximum size before a FileUploadException will be thrown
 *   upload.setSizeMax(1000000);
 *
 *   List fileItems = upload.parseRequest(req);
 *   // assume we know there are two files. The first file is a small
 *   // text file, the second is unknown and is written to a file on
 *   // the server
 *   Iterator i = fileItems.iterator();
 *   String comment = ((FileItem)i.next()).getString();
 *   FileItem fi = (FileItem)i.next();
 *   // filename on the client
 *   String fileName = fi.getName();
 *   // save comment and filename to database
 *   ...
 *   // write the file
 *   fi.write(new File("/www/uploads/", fileName));
 * }
 * </pre>
 * <p>
 * In the example above, the first file is loaded into memory as a
 * <code>String</code>. Before calling the <code>getString</code> method,
 * the data may have been in memory or on disk depending on its size. The
 * second file we assume it will be large and therefore never explicitly
 * load it into memory, though if it is less than 4096 bytes it will be
 * in memory before it is written to its final location. When writing to
 * the final location, if the data is larger than the threshold, an attempt
 * is made to rename the temporary file to the given location.  If it cannot
 * be renamed, it is streamed to the new location.
 * </p>
 */
package fd.ng.web.fileupload;
