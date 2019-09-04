package fd.ng.netserver.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;

/**
 * 本类的加载方式为： ClassUtil.loadClass(HttpServerConf.class.getName());
 */
public final class HttpServerConf extends HttpServerConfBean{
	public static final HttpServerConfBean confBean = new HttpServerConfBean();

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("httpserver")).asMap();


		confBean.setHost(rootConfig.getString("host", null));
		confBean.setHttpPort(rootConfig.getInt("port", 32100));
		confBean.setIdleTimeout(rootConfig.getInt("http.idleTimeout", 30000));
		confBean.setHttpsPort(rootConfig.getInt("httpsPort", 0));
		confBean.setWebContext(rootConfig.getString("webContext", "/fdctx"));
		confBean.setActionPattern(rootConfig.getString("actionPattern", "/action/*"));

//		// 跨域
//		CORS_Allow  = rootConfig.getBool("cors.allow", false);
//		CORS_acao   = rootConfig.getString("cors.acao", "null");
//		CORS_acam   = rootConfig.getString("cors.acam", "POST, GET");
//		CORS_acac   = rootConfig.getString("cors.acac", "true");

		// session
		YamlMap session = rootConfig.getMap("session");
		if(session!=null) {
			confBean.setSession_MaxAge(session.getInt("maxage", confBean.Session_MaxAge_Default)); // 5分钟
			confBean.setSession_HttpOnly(session.getBool("httponly", false));
		} else {
			confBean.setSession_MaxAge(confBean.Session_MaxAge_Default);
			confBean.setSession_HttpOnly(false);
		}
	}

	private HttpServerConf() { throw new AssertionError("No HttpServerConf instances for you!"); }

	public static String string() {
		return "HttpServerConf{" +
				"host=" + confBean.getHost() +
				", httpPort=" + confBean.getHttpPort() +
				", httpsPort=" + ((confBean.getHttpsPort()==0)?"":String.valueOf(confBean.getHttpsPort())) +
				", webContext=" + confBean.getWebContext() +
				", actionPattern=" + confBean.getActionPattern() +

				", Session_MaxAge=" + confBean.getSession_MaxAge() +
				", Session_HttpOnly=" + confBean.isSession_HttpOnly() +

				'}';
	}
}
