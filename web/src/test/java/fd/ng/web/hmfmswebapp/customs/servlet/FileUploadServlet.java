package fd.ng.web.hmfmswebapp.customs.servlet;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;
import fd.ng.core.utils.UuidUtil;
import fd.ng.web.helper.Loghelper;
import fd.ng.web.util.ResponseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;

public class FileUploadServlet extends HttpServlet {
	protected static final Logger logger = LogManager.getLogger(FileUploadServlet.class.getName());
	private static final long serialVersionUID = 162693266207506688L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String contentType = request.getContentType();
			if(!contentType.startsWith("multipart/form-data;")) {
				String errmsg = String.format("ERROR URI access [not multipart/form-data] : %s", request.getRequestURI());
				ResponseUtil.writeSystemError(response, errmsg);
				return;
			}
			Collection<Part> parts = request.getParts();
			for (Part part : parts) {
				String name = part.getName();
				System.out.println("----> name=" + name);
				String orgnFilename = part.getSubmittedFileName();
				String cd = part.getHeader("Content-Disposition");
				String ct = part.getHeader("Content-Type");
//			Collection<String> names = part.getHeaderNames();
//			names.forEach(System.out::println);
				long fileSize = part.getSize();
				System.out.printf("orgnFilename=%s, cd=%s, ct=%s, filesize=%d %n",
						orgnFilename, cd, ct, fileSize);
			}
		} catch (ServletException | IOException e) {
			String errCode = BaseInternalRuntimeException.ERRCODE_LOGPREFIX + UuidUtil.uuid();
			logger.error(Loghelper.fitMessage(request.getRequestURI() + errCode), e);
			ResponseUtil.writeSystemError(response, "System Internal Error." + errCode);
		}
	}
}
