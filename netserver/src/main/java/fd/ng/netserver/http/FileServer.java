package fd.ng.netserver.http;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

public class FileServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		//创建一个ResourceHandler，它处理请求的方式是提供一个资源文件
		//这是一个Jetty内置的处理器，所以它非常适合与其他处理器构成一个处理链
		ResourceHandler resourceHandler = new ResourceHandler();
		//配置ResourceHandler，设置哪个文件应该被提供给请求方
		//这个例子里，配置的是当前路径下的文件，但是实际上可以配置长任何jvm能访问到的地方
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(".");
		// 将resource_handler添加到GzipHandler中，然后将GzipHandler提供给Server
		GzipHandler gzip = new GzipHandler();
		server.setHandler(gzip);

		HandlerList handlers = new HandlerList();
		// DefaultHandler 将会为不能匹配到资源的生成一个格式良好的404回应。
		handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler() });
		gzip.setHandler(handlers);

		server.start();
		server.join();
	}
}
