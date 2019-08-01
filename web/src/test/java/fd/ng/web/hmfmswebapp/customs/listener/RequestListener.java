package fd.ng.web.hmfmswebapp.customs.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.util.Arrays;

public class RequestListener implements ServletRequestListener {
	protected static final Logger logger = LogManager.getLogger(RequestListener.class.getName());
	@Override
	public void requestInitialized(ServletRequestEvent servletRequestEvent) {
		System.out.println();
		System.out.println("RequestListener requestInitialized : " + servletRequestEvent.toString());
		System.out.println("ServletRequest data in Initialized : ");
		ServletRequest request = servletRequestEvent.getServletRequest();
		request.getParameterMap().forEach((name, values)-> System.out.println(name + "=" + Arrays.toString(values)));
	}

	@Override
	public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
		System.out.println("RequestListener requestDestroyed : " + servletRequestEvent.toString());
		System.out.println("ServletRequest data in Destroyed : ");
		ServletRequest request = servletRequestEvent.getServletRequest();
		request.getParameterMap().forEach((name, values)-> System.out.println(name + "=" + Arrays.toString(values)));
	}
}
