package fd.ng.web.hmfmswebapp.customs.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

/**
 * 仅仅是一个例子代码
 */
public class MyFilter implements Filter {
	private static final Logger logger = LogManager.getLogger();
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		long start = System.currentTimeMillis();
		logger.debug("MyFilter START !");
		servletRequest.setAttribute("__newValue", "MyFilter");
		filterChain.doFilter(servletRequest, servletResponse);
		logger.debug("MyFilter END ! time : " + (System.currentTimeMillis()-start));
	}

	@Override
	public void destroy() {

	}
}
