package fd.ng.cmdtools.loadrunner;

import fd.ng.core.utils.StringUtil;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// 不需要了
public class MyHttpTestTask extends TaskPanel {
	public MyHttpTestTask(String taskName, Map<String, Object> forThreadData) {
		super(taskName, forThreadData);
	}

	@Override
	public String executor() throws TaskException {
		try {
			System.out.println("Used user-defined Task Class : MyHttpTestTask");
			String httpMethod = (String)forThreadData.get("httpMethod");
			if("get".equalsIgnoreCase(httpMethod))
				return get();
			else if("post".equalsIgnoreCase(httpMethod))
				return post();
			else
				throw new TaskException("Unsupport http method : " + httpMethod);
		} catch (Exception e) {
			// 必须把自己的代码catch住，并抛出TaskException异常。
			// 基类通过捕获该异常，判断被压测的代码是否出错了。
			if( e instanceof TaskException)
				throw (TaskException)e;
			else
				throw new TaskException(e);

		}
	}

	private String get() throws TaskException, IOException {
		OkHttpClient httpClient = (OkHttpClient)forThreadData.get("loadrunner");
		String url = (String)forThreadData.get("url");
		if(url.indexOf("?")<0){
			url = url + "?";
			url += "DFA_taskname=" + taskName;
		} else {
			url += "&DFA_taskname=" + taskName;
		}
		url += "&DFA_thread_id=" + Long.toString(Thread.currentThread().getId());
		url += "&DFA_threadname=" + Thread.currentThread().getName();
		url += "&who=myhttp";

		Request request = new Request.Builder()
				.url(url)
				.build();
		Response response = httpClient.newCall(request).execute();
		int responseCode = response.code();
		if ( responseCode != 200 ){
			throw new TaskException("Response failed! StatusCode="+responseCode);
		}
		String val = response.body().string();
		return val;
	}
	private String post() throws TaskException, IOException {
		Map<String, String> postDataMap = (Map<String, String>)forThreadData.get("post");
		OkHttpClient httpClient = (OkHttpClient)forThreadData.get("loadrunner");
		String url = (String)forThreadData.get("url");
		FormBody.Builder builder = new FormBody.Builder();
		for(String key : postDataMap.keySet()){
			String val = postDataMap.get(key);
			builder.add(key, val);
		}
		builder.add("DFA_taskname", taskName);
		builder.add("DFA_thread_id", Long.toString(Thread.currentThread().getId()));
		builder.add("DFA_threadname", Thread.currentThread().getName());
		RequestBody body = builder.build();
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		Response response = httpClient.newCall(request).execute();
		int responseCode = response.code();
		if ( responseCode != 200 ){
			throw new TaskException("Response failed! StatusCode="+responseCode);
		}
		String val = response.body().string();
		return val;
	}

	public static Map<String, Object> buildInitTaskData(Map<String, String> cmdMap){
		if(StringUtil.isBlank(cmdMap.get("url")))
			throw new RuntimeException("You must provide CMD argument : url=http://host:port/uri");
		Map<String, Object> taskParamMap = new HashMap<>();
		// 设置 http 请求的数据项
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		httpClient.connectTimeout(15, TimeUnit.SECONDS); // 连接超时
		httpClient.readTimeout(15, TimeUnit.SECONDS); // 读取超时
		taskParamMap.put("loadrunner", httpClient.build());
		taskParamMap.put("url", cmdMap.get("url"));
		// 如果是 POST 方式，则需要从数据文件中读取post数据
		String post = cmdMap.get("post");
		if(post!=null&&post.trim().length()>1) {
			File file = new File(post);
			if(file==null) {
				throw new RuntimeException("[ " + post + " ] is not file!");
			}
			Map<String, String> postDataMap = new HashMap<>();
			try ( BufferedReader br = new BufferedReader(new FileReader(file)) ) {
				String line = null;
				while((line = br.readLine()) != null){
					if(line.trim().length()==0) continue;
					if(line.indexOf('=')<0){
						System.out.println("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
						continue;
					}
					String[] kv = line.split("=");
					if(kv.length!=2){
						System.out.println("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
						continue;
					}
//					System.out.println("line : " + line);
					postDataMap.put(kv[0], kv[1]);
				}
			} catch ( IOException e) {
				System.out.println("[ " + post + " ] can not read!");
				throw new RuntimeException(e);
			}
			taskParamMap.put("post", postDataMap);
			taskParamMap.put("httpMethod", "post");
		}
		else
			taskParamMap.put("httpMethod", "get");

		// 这些在主线程中构造的数据，不允许被修改了
		taskParamMap = Collections.unmodifiableMap(taskParamMap);
		return taskParamMap;
	}
}
