package fd.ng.web;

import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netserver.conf.HttpServerConf;
import fd.ng.test.junit.FdBaseTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebBaseTestCase extends FdBaseTestCase {
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

	// ---------  以下代码没有意义！仅仅是为了那些使用了 junit 里面 WebBaseTestCase(: OldWebBaseTestCase) 的历史遗留代码
	protected String post(String url) {
		return post(url, ( String[][])null);
	}
	protected String post(String url, Map<String, String[]> postData) {
		HttpClient httpClient = new HttpClient();
		Optional.ofNullable(postData).ifPresent(d->{
			d.forEach((k, v)->{httpClient.addData(k, v);});
		});
		return httpClient.post(url).getBodyString();
	}
	protected String post(String url, String[][] postData) {
		HttpClient httpClient = new HttpClient();
		Optional.ofNullable(postData).ifPresent(d->{
			for (String[] kv : d) {
				if (kv.length < 2) throw new RuntimeException("参数不合法，每行数据至少应该有两个值");
				String key = kv[0];
				String[] value = new String[kv.length - 1];
				for (int i = 1; i < kv.length; i++) value[i - 1] = kv[i];
				httpClient.addData(kv[0], value);
			}
		});
		return httpClient.post(url).getBodyString();
	}

	private Map<String, String[]> genPostDataMap(String[][] postData) {
		Map<String, String[]> postDataMap = new HashMap<>();
		Optional.ofNullable(postData).ifPresent(d->{
			for (String[] kv : d) {
				if (kv.length < 2) throw new RuntimeException("参数不合法，每行数据至少应该有两个值");
				String key = kv[0];
				String[] value = new String[kv.length - 1];
				for (int i = 1; i < kv.length; i++) value[i - 1] = kv[i];
				postDataMap.put(kv[0], value);
			}
		});
		return postDataMap;
	}

}
