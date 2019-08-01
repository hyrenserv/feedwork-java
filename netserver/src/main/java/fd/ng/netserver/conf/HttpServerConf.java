package fd.ng.netserver.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;

/**
 * 本类的加载方式为： ClassUtil.loadClass(HttpServerConf.class.getName());
 */
public final class HttpServerConf {
	public static final String  Host;            // 绑定的主机。如果是null或0.0.0.0，绑定到所有接口；
	public static final int     HttpPort;
	public static final int     IdleTimeout;
	public static final int     HttpsPort;
	public static final String  WebContext;
	public static final String  ActionPattern;

//	// CORS 跨域配置
//	public static final boolean CORS_Allow;
//	public static final String  CORS_acao;  // Access-Control-Allow-Origin
//	public static final String  CORS_acam;  // Access-Control-Allow-Methods
//	public static final String  CORS_acac;  // Access-Control-Allow-Credentials

	// session
	private static final int     Session_MaxAge_Default = 300; // 默认的session过期时间：5分钟。
	// session过期时间，单位是秒。这是指不活动的过期时间，也就是说，如果一直在页面做操作，就一直有效。如果5分钟都没有操作，则失效
	public static final int      Session_MaxAge;
	public static final boolean  Session_HttpOnly; // true：通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。

//	// cookie
//	public static final int     Cookie_MaxAge; // cookie过期时间，单位是秒，默认为8小时
//	public static final boolean Cookie_HttpOnly;
//	public static final String  Cookie_Path; // 默认不设置
//	public static final boolean Cookie_Secure;

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("httpserver")).asMap();

		Host            = rootConfig.getString("host", null);
		HttpPort        = rootConfig.getInt("port", 32100);
		IdleTimeout     = rootConfig.getInt("http.idleTimeout", 30000);
		HttpsPort       = rootConfig.getInt("httpsPort", 0);
		WebContext      = rootConfig.getString("webContext", "/fdctx");
		ActionPattern   = rootConfig.getString("actionPattern", "/action/*");

//		// 跨域
//		CORS_Allow  = rootConfig.getBool("cors.allow", false);
//		CORS_acao   = rootConfig.getString("cors.acao", "null");
//		CORS_acam   = rootConfig.getString("cors.acam", "POST, GET");
//		CORS_acac   = rootConfig.getString("cors.acac", "true");

		// session
		YamlMap session = rootConfig.getMap("session");
		if(session!=null) {
			Session_MaxAge      = session.getInt("maxage", Session_MaxAge_Default); // 5分钟
			Session_HttpOnly    = session.getBool("httponly", false);
		} else {
			Session_MaxAge      = Session_MaxAge_Default;
			Session_HttpOnly    = false;
		}
	}

	private HttpServerConf() { throw new AssertionError("No HttpServerConf instances for you!"); }

	public static String string() {
		return "HttpServerConf{" +
				"host=" + Host +
				", httpPort=" + HttpPort +
				", httpsPort=" + ((HttpsPort==0)?"":String.valueOf(HttpsPort)) +
				", webContext=" + WebContext +
				", actionPattern=" + ActionPattern +

				", Session_MaxAge=" + Session_MaxAge +
				", Session_HttpOnly=" + Session_HttpOnly +

				'}';
	}
}
