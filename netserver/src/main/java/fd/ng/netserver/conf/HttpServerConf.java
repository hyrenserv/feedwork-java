package fd.ng.netserver.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.yaml.*;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.DefaultDataSource;

import java.util.*;

/**
 * 本类的加载方式为： ClassUtil.loadClass(HttpServerConf.class.getName());
 */
public final class HttpServerConf extends HttpServerConfBean {
    public static HttpServerConfBean confBean = new HttpServerConfBean();
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
        for (int i = 0; i < httpserver.size(); i++) {
            YamlMap httpconf = httpserver.getMap(i);
            HttpServerConfBean confBean = new HttpServerConfBean();
            String name = httpconf.getString("name");
            if (__Httpserver.containsKey(name))
                throw new FrameworkRuntimeException("httpserver : name=" + name + " already exists !");
            confBean.setName(name);
            confBean.setHost(httpconf.getString("host", null));
            confBean.setHttpPort(httpconf.getInt("port", 32100));
            confBean.setIdleTimeout(httpconf.getInt("http.idleTimeout", 30000));
            confBean.setHttpsPort(httpconf.getInt("httpsPort", 0));
            confBean.setWebContext(httpconf.getString("webContext", "/fdctx"));
            confBean.setActionPattern(httpconf.getString("actionPattern", "/action/*"));
            //// 跨域
           /* confBean.setCORS_Allow(httpconf.getBool("cors.allow", false));
            confBean.setCORS_acao(httpconf.getString("cors.acao", "null"));
            confBean.setCORS_acam(httpconf.getString("cors.acam", "POST, GET"));
            confBean.setCORS_acac(httpconf.getString("cors.acac", "true"));*/
            if (httpconf.exist("session")) {
                YamlArray properties = httpconf.getArray("session");
                for (int j = 0; j < properties.size(); j++) {
                    String line = properties.getString(j);
                    YamlMapAnywhere oneProp = (YamlMapAnywhere) YamlFactory.getYamlReader(line).asMap();
                    for (final YamlNode key : oneProp.keys()) {
                        String keyName = ((Scalar) key).value();
                        String val = oneProp.getString(keyName);
                        confBean.addProperty(keyName, val);
                        if (keyName.equals("maxage")) {
                            confBean.setSession_MaxAge(oneProp.getInt("maxage", confBean.Session_MaxAge_Default)); // 5分钟
                        } else if (keyName.equals("httponly")) {
                            confBean.setSession_HttpOnly(oneProp.getBool("httponly", false));
                        }
                    }
                }
            } else {
                confBean.setSession_MaxAge(confBean.Session_MaxAge_Default);
                confBean.setSession_HttpOnly(false);
            }
            __Httpserver.put(name, confBean);
        }
        httpServerInfos = Collections.unmodifiableMap(__Httpserver);
        confBean = getHttpServer(DEFAULT_DBNAME);
    }

    public static HttpServerConfBean getHttpServer(String name) {
        if (name == null)
            return httpServerInfos.get(DEFAULT_DBNAME);
        else
            return httpServerInfos.get(name);
    }

    private HttpServerConf() {
        throw new AssertionError("No HttpServerConf instances for you!");
    }

    public static String string() {
        return "HttpServerConf{" +
                "host=" + confBean.getHost() +
                ", httpPort=" + confBean.getHttpPort() +
                ", httpsPort=" + ((confBean.getHttpsPort() == 0) ? "" : String.valueOf(confBean.getHttpsPort())) +
                ", webContext=" + confBean.getWebContext() +
                ", actionPattern=" + confBean.getActionPattern() +

                ", Session_MaxAge=" + confBean.getSession_MaxAge() +
                ", Session_HttpOnly=" + confBean.isSession_HttpOnly() +

                '}';
    }
}
