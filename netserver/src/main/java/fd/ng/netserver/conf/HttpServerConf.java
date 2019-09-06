package fd.ng.netserver.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.yaml.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 本类的加载方式为： ClassUtil.loadClass(HttpServerConf.class.getName());
 */
public final class HttpServerConf {
    protected static final Logger logger = LogManager.getLogger(HttpServerConf.class.getName());
	public static final HttpServerConfBean confBean;
	public static final String DEFAULT_DBNAME = "default";
	public static final Map<String, HttpServerConfBean> httpServerInfos; // 存储每一个配置的httpserver的连接信息。key为每组的name值

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("httpserver")).asMap();

		YamlArray httpserver = rootConfig.getArray("httpserver");
		if (httpserver == null) {
			throw new FrameworkRuntimeException("\nCan not found httpserver.conf, Or, missing key [ httpserver ] in httpserver.conf!" +
					"\nFor fix this problem:" +
					"\nIn dev        time: Rebuild whole project" +
					"\nIn production time: Restart yours application");
		}
		Map<String, HttpServerConfBean> __Httpserver = new HashMap<>(httpserver.size());
		final int initPort = 32100;
		final int defaultSessionMaxAge = 300; // 默认的session过期时间：5分钟。
		for (int i = 0; i < httpserver.size(); i++) {
			YamlMap httpconf = httpserver.getMap(i);
			HttpServerConfBean confBean = new HttpServerConfBean();
			String name = httpconf.getString("name");
			if (__Httpserver.containsKey(name))
				throw new FrameworkRuntimeException("httpserver : name=" + name + " already exists !");
			confBean.setName(name);
			confBean.setHost(httpconf.getString("host", null));
			confBean.setHttpPort(httpconf.getInt("port", (initPort+i)));
			if (httpconf.exist("idleTimeout"))
				confBean.setIdleTimeout(httpconf.getInt("idleTimeout"));
			if (httpconf.exist("httpsPort"))
				confBean.setHttpsPort(httpconf.getInt("httpsPort"));
			confBean.setWebContext(httpconf.getString("webContext", "/fdctx"+i));
			confBean.setActionPattern(httpconf.getString("actionPattern", "/action"+i+"/*"));
			if (httpconf.exist("session")) {
				YamlMap session = httpconf.getMap("session");
				confBean.setSession_MaxAge(session.getInt("maxage", defaultSessionMaxAge)); // 默认的session过期时间：5分钟。
				confBean.setSession_HttpOnly(session.getBool("httponly", false));
			} else {
				confBean.setSession_MaxAge(defaultSessionMaxAge); // 默认的session过期时间：5分钟。
				confBean.setSession_HttpOnly(false);
			}
			__Httpserver.put(name, confBean);
		}
		httpServerInfos = Collections.unmodifiableMap(__Httpserver);
        warning();
		confBean = getHttpServer(DEFAULT_DBNAME);
	}

	public static HttpServerConfBean getHttpServer() {
		return getHttpServer(null);
	}
	public static HttpServerConfBean getHttpServer(String name) {
		if (name == null)
			return httpServerInfos.get(DEFAULT_DBNAME);
		else
			return httpServerInfos.get(name);
	}

	// 检查是否有重复使用的配置值
	private static void warning() {
		Set<String> hosts = new HashSet<>(httpServerInfos.size());
		Set<Integer> ports = new HashSet<>(httpServerInfos.size());
		Set<Integer> httpsPorts = new HashSet<>(httpServerInfos.size());
		Set<String> webContexts = new HashSet<>(httpServerInfos.size());

		for(Map.Entry<String, HttpServerConfBean> entry : httpServerInfos.entrySet()) {
			String curName = entry.getKey();
			HttpServerConfBean conf = entry.getValue();

			if(hosts.contains(conf.getHost()))
				logger.warn(curName + " --> duplicate host : " + conf.getHost());
			else hosts.add(conf.getHost());

			if(ports.contains(conf.getHttpPort()))
				logger.warn(curName + " --> duplicate port : " + conf.getHttpPort());
			else ports.add(conf.getHttpPort());

			if(httpsPorts.contains(conf.getHttpsPort()))
				logger.warn(curName + " --> duplicate httpsPort : " + conf.getHttpsPort());
			else httpsPorts.add(conf.getHttpsPort());

			if(webContexts.contains(conf.getWebContext()))
				logger.warn(curName + " --> duplicate webContext : " + conf.getWebContext());
			else webContexts.add(conf.getWebContext());
		}
	}

	private HttpServerConf() {
		throw new AssertionError("No HttpServerConf instances for you!");
	}

	public static String string() {
		return "HttpServerConf{" + httpServerInfos +
				"}";
	}
}
