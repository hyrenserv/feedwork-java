package fd.ng.netclient.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * netclientinfo.conf可以没有，全部使用默认值即可
 */
public class NetclientConf {
	private static final Logger logger = LogManager.getLogger(NetclientConf.class.getName());

	public static final long    ConnectTimeout;
	public static final long    ReadTimeout;
	public static final long    WriteTimeout;
	public static final boolean RetryOnConnectionFailure;
	public static final boolean HasCookie;
	static {
		// 如果新增一种类型（比如 rpc client），那么同样的处理方式：
		// 1) try 外面定义 rpcConfig
		// 2）在“if(rootConfig != null)” 里面取值，在catch中赋值成null
		// 3）下面类似httpConfig一样增加与其对应的各个配置像取值的 if ... else
		YamlMap httpConfig;
		try {
			YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("netclientinfo")).asMap();
			if(rootConfig == null) {
				httpConfig = null;
			} else {
				httpConfig = rootConfig.getMap("http");
			}
		} catch (YamlFactory.YamlFileNotFoundException e) {
			httpConfig = null;
		}
		if(httpConfig==null) {
			ConnectTimeout   = 10;
			ReadTimeout      = 10;
			WriteTimeout     = 10;
			RetryOnConnectionFailure = true;
			HasCookie        = true;
		}
		else {
			ConnectTimeout   = httpConfig.getInt("connectTimeout", 10);
			ReadTimeout      = httpConfig.getInt("readTimeout", 10);
			WriteTimeout     = httpConfig.getInt("writeTimeout", 10);
			RetryOnConnectionFailure = httpConfig.getBool("retryOnConnectionFailure", true);
			HasCookie        = httpConfig.getBool("hasCookie", true);
		}
	}
}
