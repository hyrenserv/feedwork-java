package ${basePackage}.testbase;

import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netserver.conf.HttpServerConf;
import fd.ng.test.junit.FdBaseTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ${className} extends FdBaseTestCase {
	protected String getHost() {
		return (StringUtil.isBlank(HttpServerConf.Host)?"localhost": HttpServerConf.Host);
	}
	protected String getPort() {
		return String.valueOf(HttpServerConf.HttpPort);
	}
	protected String getHostPort() {
		String ActionPattern = HttpServerConf.ActionPattern;
		if(ActionPattern.endsWith("/*")) ActionPattern = ActionPattern.substring(0, ActionPattern.length()-2);
		return "http://"+ getHost()	+ ":" + getPort();
	}
	protected String getUrlCtx() {
		return getHostPort() + HttpServerConf.WebContext;
	}
	protected String getUrlActionPattern() {
		String ActionPattern = HttpServerConf.ActionPattern;
		if(ActionPattern.endsWith("/*")) ActionPattern = ActionPattern.substring(0, ActionPattern.length()-2);
		return getUrlCtx() + ActionPattern;
	}
	protected String getActionPath() {
		return getUrlActionPattern()
				+ "/" + this.getClass().getPackage().getName().replace(".", "/");
	}
	protected String getActionUrl(String actionMethodName) {
		return getActionPath() + "/" + actionMethodName;
	}
}
