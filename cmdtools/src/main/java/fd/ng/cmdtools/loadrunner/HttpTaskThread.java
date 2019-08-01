package fd.ng.cmdtools.loadrunner;

import fd.ng.core.utils.*;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpTaskThread extends TaskPanel {
	private static final Logger logger = LogManager.getLogger(HttpTaskThread.class.getName());
	protected static final OkHttpClient httpClient;
	protected static final ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
	// 存储服务器返回的Session信息，以便提交数据时能够带着Session。

	private final AtomicInteger seqno;

	static {
		OkHttpClient.Builder hcBuilder = new OkHttpClient.Builder()
				.connectTimeout(25, TimeUnit.SECONDS) // 连接超时
				.readTimeout(25, TimeUnit.SECONDS) // 读取超时
				.retryOnConnectionFailure(true)
				.cookieJar(new CookieJar() {
					@Override
					public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
						cookieStore.put(url.host(), cookies);
					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl url) {
						List<Cookie> cookies = cookieStore.get(url.host());
						return cookies != null ? cookies : new ArrayList<Cookie>(0);
					}
				});
		httpClient = hcBuilder.build();
	}
	public HttpTaskThread(String taskName, Map<String, Object> forThreadData) {
		super(taskName, forThreadData);
		seqno = new AtomicInteger(0);
	}

	@Override
	public String executor() throws TaskException {
		try {
			String httpMethod = (String)forThreadData.get("httpMethod");
			String loginUrl = (String)forThreadData.get("loginUrl");
			String sessionid = null;
			if(StringUtil.isNotEmpty(loginUrl)) {
				List<Map<String, String>> loginData = (List<Map<String, String>>)forThreadData.get("loginData");
				sessionid = login(loginUrl, loginData);
			}
			if("get".equalsIgnoreCase(httpMethod))
				return get();
			else if("post".equalsIgnoreCase(httpMethod))
				return post(sessionid);
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
//		OkHttpClient httpClient = (OkHttpClient)forThreadData.get("loadrunner");
		String url = (String)forThreadData.get("url");
		if(!url.contains("?")){
			url += "?randomid=" + UuidUtil.uuid();
		} else {
			url = genArgValue(url);
		}

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

	private String post(String sessionid) throws TaskException, IOException {
		Map<String, String> postDataMap = (Map<String, String>)forThreadData.get("postData");
//		OkHttpClient httpClient = (OkHttpClient)forThreadData.get("loadrunner");
		String url = (String)forThreadData.get("url");
		FormBody.Builder builder = new FormBody.Builder();
		for(String key : postDataMap.keySet()){
			String val = postDataMap.getOrDefault(key, "");

			builder.add(key, genArgValue(val));
		}
		RequestBody body = builder.build();
		Request.Builder reqBuilder = new Request.Builder()
				.url(url)
				.post(body);
		if(sessionid!=null) {
			logger.trace("[POST] add JSESSIONID("+sessionid+") to header");
			reqBuilder = reqBuilder.addHeader("cookie", sessionid);
		}
		Request request = reqBuilder.build();

		Response response = httpClient.newCall(request).execute();
		int responseCode = response.code();
		if ( responseCode != 200 ){
			throw new TaskException("[POST] response failed! StatusCode="+responseCode);
		}
		String val = response.body().string();

		return val;
	}

	/**
	 *
	 * @param url 登陆地址
	 * @param loginData 登陆用户数据清单
	 * @return 登陆成功，返回 JSESSIONID
	 * @throws TaskException
	 * @throws IOException
	 */
	private String login(String url, List<Map<String, String>> loginData) throws TaskException, IOException {
		// 随机获取一个用户
		int userNums = loginData.size();
		int index = (int)(Thread.currentThread().getId()%userNums);
		Map<String, String> userinfo = loginData.get(index);

		// 构造提交的数据
		FormBody.Builder builder = new FormBody.Builder();
		builder.add(userinfo.get("username.key"), genArgValue(userinfo.get("username.val")));
		builder.add(userinfo.get("password.key"), genArgValue(userinfo.get("password.val")));
		RequestBody body = builder.build();

		Request.Builder reqBuilder = new Request.Builder()
				.url(url)
				.post(body);
		Request request = reqBuilder.build();

		Response response = httpClient.newCall(request).execute();
		int responseCode = response.code();
		if ( responseCode != 200 ){
			throw new TaskException("Login response failed! StatusCode="+responseCode);
		}

		Headers headers = response.headers(); // 登陆成功，已经有了 JSESSIONID
		logger.trace("[Login] url = " + url);
		logger.trace("[Login] headers = [" + headers + "]");
		List<String> cookies = headers.values("Set-Cookie");
		for (String cookieStr : cookies) {
			if (cookieStr.startsWith("JSESSIONID=")) {
				int loc = cookieStr.indexOf(';');
				if (loc > 11) {
					String coSessionid = cookieStr.substring(0, loc);
					logger.trace("[Login] get session : " + coSessionid);
					return coSessionid;
				}
			}
		}
		throw new RuntimeException("Cannot found JSESSIONID! Headers :\n" + headers.toString());
	}
	/**
	 * 本函数用于构造任务线程共享的全局对象。比如 HTTPCLIENT等。
	 * 这里构造出来的对象，会在主线程中调用，并传给任务线程的forThreadData参数
	 *
	 * @param cmdMap Map<String, String> 命令行参数转成了k,v格式存储。命令行参数必须是name=value的形式
	 *               url=访问地址           : 不能为空
	 *               postData=文件全路径    : POST方式提交的数据（以name=value的形式逐行填写）。如果为空，则以 GET 方式提交
	 *               loginUrl=登陆地址      : 登陆验证的地址
	 * @return
	 */
	public static Map<String, Object> buildInitTaskData(Map<String, String> cmdMap) {
		if(StringUtil.isBlank(cmdMap.get("url")))
			throw new RuntimeException("You must provide CMD argument : url=http://host:port/uri");
		Map<String, Object> taskParamMap = new HashMap<>();
		taskParamMap.put("url", cmdMap.get("url"));

		// 处理登陆场景数据
		String loginUrl = cmdMap.get("loginUrl"); // 用于登陆验证的url
		if(StringUtil.isNotEmpty(loginUrl)) {
			final String loginDataFile = cmdMap.computeIfAbsent("loginData", k->{throw new RuntimeException("cmd arg 'loginData' must not null");});
			Path file = Paths.get(loginDataFile);
			if(!file.toFile().isFile())
				throw new RuntimeException("[ " + loginDataFile + " ] is not file!");

			// 读取登陆用户数据清单
			List<Map<String, String>> userDataList = new ArrayList<>();
			try {
				// 每行一组用户名/密码，数据格式为： 用户名=值&密码=值。会随机取一组用于用户登陆
				// 必须是用户名在前面，密码在后面
				// 值可以使用占位参数，见 genArgValue
				Files.readAllLines(file).forEach(line->{
					if(line.trim().length()>0) {
						if(line.indexOf('&')<0) logger.warn("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
						else {
							String[] kv = line.split("&");
							if(kv.length!=2) logger.warn("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
							else {
								Map<String, String> userpwd = new HashMap<>();
								String[] userinfo = kv[0].split("=");
								if(userinfo.length!=2) logger.warn("data line : [ " + line + " ]' username is wrong, must be [ name=value ]!");
								else {
									userpwd.put("username.key", userinfo[0]);
									userpwd.put("username.val", userinfo[1]);
								}
								String[] pwdinfo = kv[1].split("=");
								if(pwdinfo.length!=2) logger.warn("data line : [ " + line + " ]' password is wrong, must be [ name=value ]!");
								else {
									userpwd.put("password.key", pwdinfo[0]);
									userpwd.put("password.val", pwdinfo[1]);
								}
								if(userpwd.size()==4) userDataList.add(userpwd);
							}
						}
					}
				});
				if(userDataList.size()<1) throw new RuntimeException("loginDataFile[ " + loginDataFile + " ] read nothing");
			} catch ( IOException e) {
				throw new RuntimeException("loginDataFile[ " + loginDataFile + " ] read failed", e);
			}
			taskParamMap.put("loginUrl", loginUrl);
			taskParamMap.put("loginData", userDataList);
		}

		// 如果是 POST 方式，则需要从数据文件中读取post数据
		// 数据格式： 每行一个名值对（ name=value ）
		// 值可以使用占位参数，见 genArgValue
		final String postDataFile = cmdMap.get("postData");
		if(StringUtil.isNotEmpty(postDataFile)) {
			File file = new File(postDataFile);
			if(!file.isFile()) {
				throw new RuntimeException("[ " + postDataFile + " ] is not file!");
			}
			// 读取POST的数据
			Map<String, String> postDataMap = new HashMap<>();
			try ( BufferedReader br = new BufferedReader(new FileReader(file)) ) {
				String line = null;
				while((line = br.readLine()) != null){
					if(line.trim().length()==0) continue;
					if(line.indexOf('=')<0){
						logger.warn("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
						continue;
					}
					String[] kv = line.split("=");
					if(kv.length!=2){
						logger.warn("data line : [ " + line + " ] is wrong, must be [ name=value ]!");
						continue;
					}
					postDataMap.put(kv[0], kv[1]);
				}
			} catch ( IOException e) {
				throw new RuntimeException("postDataFile[ " + postDataFile + " ] read failed", e);
			}
			taskParamMap.put("postData", postDataMap);
			taskParamMap.put("httpMethod", "post");
		}
		else
			taskParamMap.put("httpMethod", "get");

		// 这些在主线程中构造的数据，不允许被修改了
		taskParamMap = Collections.unmodifiableMap(taskParamMap);
		return taskParamMap;
	}

	private String genArgValue(String val) {
		if(val==null) return "";
		val = val.replace("{date}", DateUtil.getSysDate());
		val = val.replace("{time}", DateUtil.getSysTime());
		val = val.replace("{datetime}", DateUtil.getDateTime());
		val = val.replace("{uuid}", UuidUtil.uuid());
		val = val.replace("{thid}", String.valueOf(Thread.currentThread().getId()));
		val = val.replace("{seqno}", String.valueOf(seqno.incrementAndGet()));
		val = val.replace("{taskname}", taskName);
		if(val.contains("{random}")) {
			final ThreadLocalRandom rand = ThreadLocalRandom.current();
//			Random rand = new Random(System.currentTimeMillis());
			val = val.replace("{random}", String.valueOf(rand.nextInt(999989)+10));
		}
		return val;
	}
}
