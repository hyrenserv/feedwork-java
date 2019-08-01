package fd.ng.web;

import fd.ng.netserver.conf.HttpServerConf;
import fd.ng.netserver.http.WebServer;
import fd.ng.web.hmfmswebapp.customs.filter.MyFilter;
import fd.ng.web.hmfmswebapp.customs.filter.WrapFdwebFilter;
import fd.ng.web.hmfmswebapp.customs.listener.InitListener;
import fd.ng.web.hmfmswebapp.customs.listener.RequestListener;
import fd.ng.web.hmfmswebapp.customs.servlet.MyServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.servlet.ListenerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class TestStageWebServerLauncher extends WebServer {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		TestStageWebServerLauncher webServer = new TestStageWebServerLauncher();
//		webServer.init();
//		webServer.config();
//		webServer.start();
		webServer.running();
	}

	@Override
	protected void addServlet(ServletContextHandler contextApp) {
		contextApp.addServlet(MyServlet.class, "/my/*");

		// 文件上传
//		ServletHolder fileUploadHolder = new ServletHolder(new FileUploadServlet());
//		ServletHolder fileUploadHolder = new ServletHolder(new WebServlet(WebServlet.DealType_FileUpload));
//		fileUploadHolder.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp"));
//		contextApp.addServlet(fileUploadHolder, "/fileupload/*");
	}

	@Override
	protected void addFilter(ServletContextHandler contextApp) {
		contextApp.addFilter(MyFilter.class,"/my/*", EnumSet.of(DispatcherType.REQUEST));
		contextApp.addFilter(WrapFdwebFilter.class, HttpServerConf.ActionPattern, EnumSet.of(DispatcherType.REQUEST));
	}

	@Override
	protected void addListener(ServletContextHandler contextApp) {
		contextApp.getServletHandler().addListener(new ListenerHolder(InitListener.class));
		contextApp.getServletHandler().addListener(new ListenerHolder(RequestListener.class));
	}
}
