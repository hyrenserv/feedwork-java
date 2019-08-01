package fd.ng.netserver.http.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContextRootHandler extends AbstractHandler {
	protected static final Logger logger = LogManager.getLogger();
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.debug("target : {}", target);
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		if(target.equals("/")) {
			response.getWriter().println("Welcome.");
		}
		else if(target.equals("/favicon.ico")) {
			logger.debug("target is favicon.ico,");
		}
		else {
			response.getWriter().println("404");
		}
		baseRequest.setHandled(true);
	}
}
