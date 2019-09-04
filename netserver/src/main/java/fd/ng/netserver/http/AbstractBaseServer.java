package fd.ng.netserver.http;

import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.netserver.conf.HttpServerConfBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public abstract class AbstractBaseServer {
	protected static final Logger logger = LogManager.getLogger();
	private static Server server;
	protected final String serverName;
	protected static HttpServerConfBean confBean;
	public AbstractBaseServer(HttpServerConfBean confBean) {
		this.serverName = "Web Server";
		this.confBean = confBean;
	}
	public AbstractBaseServer(String serverName,HttpServerConfBean confBean) {
		if(StringUtil.isBlank(serverName))
			this.serverName = "Web Server";
		else
			this.serverName = serverName;
		this.confBean = confBean;
	}
	/**
	 * 第一步：执行初始化操作
	 */
	public void init() {
		String startMessage = String.format("USE SYSTEM %s %s %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
		if(logger.isInfoEnabled()) logger.info(startMessage);
		else System.out.println(startMessage);
		startMessage = String.format("USE    JDK %s %s", System.getProperty("java.version"), System.getProperty("java.vm.specification.name"));
		if(logger.isInfoEnabled()) logger.info(startMessage);
		else System.out.println(startMessage);
		logger.info("startup ...");
		try {
			doInit();
			logger.info("Initialize Done.");
		} catch ( Exception e ) {
			logger.error(e);
			System.exit(1);
		}
	}

	/**
	 * 第二步：配置 server
	 */
	public void config() {
		try {
			server = new Server();
			//设置在JVM退出时关闭Jetty的钩子
			//这样就可以在整个功能测试时启动一次Jetty,然后让它在JVM退出时自动关闭
			server.setStopAtShutdown(true);
			configueConnector(server);
			doConfigureHandler(server);
			logger.info("Configurate Done.");
		} catch ( Exception e ) {
			logger.error(e);
			System.exit(1);
		}
	}

	/**
	 * 第三步：启动 server
	 */
	public void start() {
		try {
			String host = ( (StringUtil.isEmpty(confBean.getHost()))?"localhost": confBean.getHost() );
			logger.info("Starting server for {}:{}{}{}", host, confBean.getHttpPort(), confBean.getWebContext(), confBean.getActionPattern());
			server.start();
			String startMessage = String.format("%s started successfully at %s", this.serverName, DateUtil.getDateTime(DateUtil.DATETIME_ZHCN));
			if(logger.isInfoEnabled()) logger.info(startMessage);
			else System.out.println(startMessage);
			server.join();
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.error(e);
			System.exit(1);
		}
	}

	/**
	 * 一次性完成启动操作
	 */
	public void running() {
		init();
		config();
		start();
	}
	public void stop() {
		try {
			if (server != null) {
				server.stop();
				logger.info("Web Server stop success at {}", DateUtil.getDateTime());
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	protected abstract void doInit() throws Exception;

	/**
	 * 如果需要定制话配置 Connector ，子类中重载本方法。
	 * @param server
	 * @throws Exception
	 */
	protected void configueConnector(Server server) {
		ServerConnector connector = new ServerConnector(server);
		System.out.printf("Connector : ");

		connector.setHost("localhost");
		System.out.printf(" Host=localhost");

		connector.setPort(8086);
		System.out.printf(" HttpPort=8086");

		connector.setIdleTimeout(30000);
		System.out.printf(" IdleTimeout=30000");

		System.out.printf("%n");

		/**
		 * 据说：解决Windows下重复启动Jetty不报告端口冲突的问题。
		 * 在Windows下有个Windows + Sun的connector实现的问题：reuseAddress=true时重复启动同一个端口的Jetty不会报错。
		 * 所以必须设为false。代价是若上次退出不干净(比如有TIME_WAIT),会导致新的Jetty不能启动。
		 */
		//connector.setReuseAddress(false);
		server.addConnector(connector);
	}
	protected abstract void doConfigureHandler(Server server);
}
