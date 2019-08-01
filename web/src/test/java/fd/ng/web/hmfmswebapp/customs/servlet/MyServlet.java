package fd.ng.web.hmfmswebapp.customs.servlet;

import fd.ng.core.utils.JsonUtil;
import fd.ng.web.action.ActionResultHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyServlet extends HttpServlet {
	@Override
	protected void doPost( HttpServletRequest request,
	                      HttpServletResponse response ) throws ServletException,
			IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>DumpServlet</h1>");
		sb.append("<pre>");
		sb.append("requestURI=" + request.getRequestURI());
		sb.append("requestURL=" + request.getRequestURL().toString());
		sb.append("contextPath=" + request.getContextPath());
		sb.append("servletPath=" + request.getServletPath());
		sb.append("pathInfo=" + request.getPathInfo());
		sb.append("session=" + request.getSession(true).getId());

		String name = request.getParameter("name");
		if (name != null) {
			sb.append("name=" + name);
		}

		String newValue = (String)request.getAttribute("__newValue");
		if (newValue != null) {
			sb.append("newValue=" + newValue + ", by MyFilter");
		}

		sb.append("</pre>");

		response.setContentType("application/json; charset=utf-8");
		response.setCharacterEncoding("UTF-8"); // 防止中文乱码
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();

		out.println(JsonUtil.toJson( ActionResultHelper.success(sb.toString()) ));
	}
}
