package fd.ng.netclient.http;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.conf.NetclientConf;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 使用例子：
 * <pre>
 * 普通访问：
 * HttpClient httpClient = new HttpClient()
 * 		.addData("username", "xxx")
 * 		.addData("age", "23");
 * HttpClient.ResponseValue resVal = httpClient.post("http://......");
 *
 * 文件上传：（ 继续使用上面的 httpClient 对象，则需要先 reset() ）
 * resVal = httpClient.reset(SubmitMediaType.MULTIPART)
 * 		.addData("userid", "u1")
 * 		.addData("addr", "北京路222号 A座")
 * 		.addData("filelabel", "333")
 * 		.addFile("filename", "d:\\1.txt")
 * 		.addFile("filename", "d:\\2.jpg")
 * 		.post("http://......");
 * </pre>
 */
public class HttpClient {
	private static final Logger logger = LogManager.getLogger(HttpClient.class.getName());
	private static final MediaType JSON_MEDIATYPE = MediaType.parse("application/json; charset=utf-8");
	private static final MediaType MULTIPART_MEDIATYPE = MediaType.parse("multipart/form-data");

	// 存储服务器返回的Cookie信息，以便提交数据时能够带着Cookie。
	// TODO 应该用 ThreadLocal 吧，否则就是全局共享了，会不会越来越多占着内存不放？
	protected static final ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
	// 存储服务器返回的Session信息，以便提交数据时能够带着Session。
	// 因为Junit是对每个方法重建一个对象，所以成员变量不会在多个方法间共享
	// 所以，login作用的测试方法要首先执行，从而得到了_JSESSIONID_STRING，后面再执行具体的测试方法时，也就拿到了这个值，并addHeader再提交
	private static final ThreadLocal<String> _JSESSIONID_STRING = new ThreadLocal<>();

	protected static OkHttpClient httpClient;

	static {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(NetclientConf.ConnectTimeout, TimeUnit.SECONDS)   // 连接超时，设置这么大是为了断点跟踪时有足够的思考时间
				.readTimeout(NetclientConf.ReadTimeout, TimeUnit.SECONDS)      // 读取超时
				.writeTimeout(NetclientConf.WriteTimeout, TimeUnit.SECONDS)
				.retryOnConnectionFailure(NetclientConf.RetryOnConnectionFailure);
		if(NetclientConf.HasCookie)
			builder.cookieJar(new CookieJar() {
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
		httpClient = builder.build();
	}

	private FormBody.Builder formBuilder;
	private MultipartBody.Builder multiFormbuilder;
	// key: json name, value: String等主类型 或 List<String>等主类型列表
	private Map<String, Object> jsonData;
	private String jsonStr = null;
	private SubmitMediaType mediaType;
	private boolean isBuildSession;

	public HttpClient() {
		buildHttpClient(SubmitMediaType.FORM);
	}
	public HttpClient(SubmitMediaType mediaType) {
		buildHttpClient(mediaType);
	}
	private void buildHttpClient(SubmitMediaType mediaType) {
		this.isBuildSession = false;
		this.mediaType = mediaType;
		if(mediaType==SubmitMediaType.FORM)
			this.formBuilder = new FormBody.Builder();
		else if(mediaType==SubmitMediaType.MULTIPART)
			this.multiFormbuilder=new MultipartBody.Builder().setType(MultipartBody.FORM);
		else if(mediaType==SubmitMediaType.JSON) {
			if(this.jsonData!=null) this.jsonData.clear();
			this.jsonData = new HashMap<>();
			this.jsonStr = null;
		}
		else if(mediaType==SubmitMediaType.XML)
			throw new FrameworkRuntimeException("Unsupported Media Type : " + mediaType);
		else
			throw new FrameworkRuntimeException("Wrong Media Type : " + mediaType);
	}
	public HttpClient addData(String name, int val) {
		return addData(name, Integer.toString(val));
	}
	public HttpClient addData(String name, int[] vals) {
		for (int val : vals) addData(name, Integer.toString(val));
		return this;
	}
	public HttpClient addData(String name, long val) {
		return addData(name, Long.toString(val));
	}
	public HttpClient addData(String name, long[] vals) {
		for (long val : vals) addData(name, Long.toString(val));
		return this;
	}
	public HttpClient addData(String name, String val) {
		if(mediaType==SubmitMediaType.FORM)
			formBuilder.add(name, val);
		else if(mediaType==SubmitMediaType.MULTIPART)
			multiFormbuilder.addFormDataPart(name, val);
		else if(mediaType==SubmitMediaType.JSON)
			addJsonData(name, val);
		else if(mediaType==SubmitMediaType.XML)
			throw new FrameworkRuntimeException("Unsupported Media Type : " + mediaType);
		else
			throw new FrameworkRuntimeException("Wrong Media Type : " + mediaType);
		return this;
	}
	public HttpClient addData(String name, String[] vals) {
		for (String val : vals) addData(name, val);
		return this;
	}
	public HttpClient addFile(String name, String filename) {
		if(mediaType!=SubmitMediaType.MULTIPART) throw new FrameworkRuntimeException("addFile only for Multipart!");
		addFormFile(name, new File(filename));
		return this;
	}
	public HttpClient addFile(String name, String[] filenames) {
		if(mediaType!=SubmitMediaType.MULTIPART) throw new FrameworkRuntimeException("addFile only for Multipart!");
		for(String filename : filenames)
			addFormFile(name, new File(filename));
		return this;
	}
	public HttpClient addFile(String name, File file) {
		if(mediaType!=SubmitMediaType.MULTIPART) throw new FrameworkRuntimeException("addFile only for Multipart!");
		addFormFile(name, file);
		return this;
	}
	public HttpClient addFile(String name, File[] files) {
		if(mediaType!=SubmitMediaType.MULTIPART) throw new FrameworkRuntimeException("addFile only for Multipart!");
		for(File file : files)
			addFormFile(name, file);
		return this;
	}
	private void addFormFile(String name, File file) {
		RequestBody fileBody = RequestBody.create(mediaType.mediaType(), file);
		multiFormbuilder.addFormDataPart(name, CodecUtil.encodeURL(file.getName()), fileBody);
	}
	private void addJsonData(String name, String val) {
		Object existVal = jsonData.get(name);
		if(existVal==null) jsonData.put(name, val); // 从来没有添加过该 name 的值
		else {
			if(existVal instanceof String) {
				List<String> newVals = new ArrayList<>();
				newVals.add((String)existVal);
				newVals.add(val);
				jsonData.remove(name);
				jsonData.put(name, newVals);
			} else if(existVal instanceof List) {
				((List<String>)existVal).add(val);
			} else {
				throw new FrameworkRuntimeException(
						String.format("Wrong exist value type ! name=%s, value=%s, exist value type=%s",
								name, val, existVal.getClass()));
			}
		}
	}

	/**
	 * 这个方法与addData互斥。如果使用了本方法，则通过 addData 添加的数据被忽略
	 * @param json
	 * @return
	 */
	public HttpClient addJson(String json) {
		jsonData.clear();
		jsonStr = json;
		return this;
	}

	/**
	 * 提交时，用于建立服务端 session
	 * 一般在登录验证的功能中，使用本方法
	 *
	 * @return ResponseValue
	 */
	public HttpClient buildSession() {
		this.isBuildSession = true;
		return this;
	}

	/**
	 * POST方式访问 url
	 *
	 * @param url 访问地址
	 * @return ResponseValue
	 */
	public ResponseValue post(final String url) {
		return doPost(url);
	}

	private ResponseValue doPost(final String url) {
		try {
			RequestBody body;
			if(mediaType==SubmitMediaType.FORM)
				body = formBuilder.build();
			else if(mediaType==SubmitMediaType.MULTIPART)
				body = multiFormbuilder.build();
			else if(mediaType==SubmitMediaType.JSON) {
				if(jsonStr==null) {
					jsonStr = JsonUtil.toJson(jsonData);
				}
				body = FormBody.create(JSON_MEDIATYPE, jsonStr);
			}
			else if(mediaType==SubmitMediaType.XML)
				throw new FrameworkRuntimeException("Unsupported Media Type : " + mediaType);
			else
				throw new FrameworkRuntimeException("Wrong Media Type : " + mediaType);

			Request.Builder reqBuilder = new Request.Builder()
					.url(url)
					.post(body);

			String curSessionid = _JSESSIONID_STRING.get();
			if(StringUtil.isNotEmpty(curSessionid)) {
				logger.trace("tid={} | request with session url:[{}] sessionid={}", Thread.currentThread().getId(), url, curSessionid);
				reqBuilder = reqBuilder.addHeader("cookie", curSessionid);
			}
			Request request = reqBuilder.build();

			Response response = httpClient.newCall(request).execute();
			if(isBuildSession) {
				if(response.isSuccessful()) {
					Headers headers = response.headers();
					if(logger.isTraceEnabled()) logger.trace("tid={} | request for Session, url=[{}] Header :\n{}", Thread.currentThread().getId(), url, headers);
					List<String> cookies = headers.values("Set-Cookie");
					for (String cookieStr : cookies) {
						if (cookieStr.startsWith("JSESSIONID=")) {
							int loc = cookieStr.indexOf(';');
							if (loc > 11) {
								String coSessionid = cookieStr.substring(0, loc);
								if(logger.isTraceEnabled()) {
									logger.trace("tid={} | new sessionid={}", Thread.currentThread().getId(), coSessionid);
								}
								_JSESSIONID_STRING.set(cookieStr.substring(0, loc));
								break;
							}
						}
					}
				}
			}
			int responseCode = response.code();

			ResponseBody resBody = response.body();
			String bodyString;
			if(resBody==null) {
				logger.error("url [ {} ] response body is null!", url);
				bodyString = StringUtil.EMPTY;
			} else {
				bodyString = resBody.string();
			}
			return new ResponseValue(responseCode, bodyString);
		} catch (IOException e) {
			logger.error("url [ " + url + " ] execute() failed! ", e);
			return new ResponseValue(-1, null);
		} finally {
			// 一次访问结束后，清理数据，防止使用同一个HttpClient对象反复提交数据
			// 如果继续使用同一个 HttpClient 对象，需先调用 reset() 方法
			multiFormbuilder = null;
			formBuilder = null;
			if(jsonData!=null) jsonData.clear();
			jsonStr = null;
			isBuildSession = false;
		}
	}

	/**
	 * 重置 HttpClient 对象成普通方式。
	 *
	 * @return HttpClient
	 */
	public HttpClient reset() {
		buildHttpClient(SubmitMediaType.FORM);
		return this;
	}
	public HttpClient reset(SubmitMediaType mediaType) {
		buildHttpClient(mediaType);
		return this;
	}
	public SubmitMediaType getMediaType() { return this.mediaType; }

//	/**
//	 * 访问，并且创建 SESSION 。
//	 * 一般在登录验证时，使用本方法。
//	 *
//	 * @param url
//	 * @param formItems post的数据，名值对，值为String[]，即使只有1个值也要用String[]。
//	 * @return ResponseValue 包含响应状态码和返回的数据
//	 */
//	public static ResponseValue postAndCreateSession(final String url, Map<String, String[]> formItems) {
//		return doPost(url, true, formItems, null);
//	}
//	/**
//	 * 访问，并且创建 SESSION 。
//	 * 一般在登录验证时，使用本方法。
//	 * 这种参数与Map参数的唯一区别，仅仅是为了在Java代码中书写方便。
//	 *
//	 * @param url
//	 * @param formItems post的数据。二维String数组，每行是一个名值对，第1个是名，后面是多个值。
//	 * @return ResponseValue 包含响应状态码和返回的数据
//	 */
//	public static ResponseValue postAndCreateSession(final String url, String[][] formItems) {
//		return doPost(url, true, genPostDataMap(formItems), null);
//	}
//	/**
//	 * 访问，并且创建 SESSION 。
//	 * 一般在登录验证时，使用本方法。
//	 * 这种参数与Map参数的唯一区别，仅仅是为了在Java代码中书写方便。
//	 *
//	 * @param url
//	 * @param formItems post的数据。名值对，值为String[]，即使只有1个值也要用String[]。
//	 * @param fileItems post的文件。值为String[]，即使只有1个值也要用String[]。值为文件全路径名。
//	 * @return ResponseValue 包含响应状态码和返回的数据
//	 */
//	public static ResponseValue postAndCreateSession(final String url, Map<String, String[]> formItems, Map<String, String[]> fileItems) {
//		return doPost(url, true, formItems, null);
//	}
//	/**
//	 * 访问，并且创建 SESSION 。
//	 * 一般在登录验证时，使用本方法。
//	 * 这种参数与Map参数的唯一区别，仅仅是为了在Java代码中书写方便。
//	 *
//	 * @param url
//	 * @param formItems post的数据。二维String数组，每行是一个名值对，第1个是名，后面是多个值。
//	 * @param fileItems post的文件。二维String数组，每行是一个名值对，第1个是名，后面是多个值，值为文件全路径名。
//	 * @return ResponseValue 包含响应状态码和返回的数据
//	 */
//	public static ResponseValue postAndCreateSession(final String url, String[][] formItems, String[][] fileItems) {
//		return doPost(url, true, genPostDataMap(formItems), genPostDataMap(fileItems));
//	}
//
////	/**
////	 * 仅访问指定的 url，没有提交的数据
////	 * @param url
////	 * @return
////	 */
////	public static ResponseValue post(String url) {
////		return doPost(url, false, null, null);
////	}
//	/**
//	 *
//	 * @param url 请求的URL
//	 * @param formItems 提交的数据。每行是一个名值组合，第一个是名字，后面的是值，可以有多个
//	 * @return 返回 Action 方法的返回数据
//	 */
//	public static ResponseValue post(String url, String[][] formItems){
//		return doPost(url, false, genPostDataMap(formItems), null);
//	}
//	public static ResponseValue post(String url, String[][] formItems, String[][] fileItems){
//		return doPost(url, false, genPostDataMap(formItems), genPostDataMap(fileItems));
//	}
//	public static ResponseValue post(String url, Map<String, String[]> formItems) {
//		return doPost(url, false, formItems, null);
//	}
//	public static ResponseValue post(String url, Map<String, String[]> formItems, Map<String, String[]> fileItems) {
//		return doPost(url, false, formItems, fileItems);
//	}
//
//	private static Map<String, String[]> genPostDataMap(String[][] postData) {
//		Map<String, String[]> postDataMap = new HashMap<>();
//		Optional.ofNullable(postData).ifPresent(d->{
//			for (String[] kv : d) {
//				if (kv.length < 2) throw new RuntimeException("参数不合法，每行数据至少应该有两个值");
//				String key = kv[0];
//				String[] value = new String[kv.length - 1];
//				for (int i = 1; i < kv.length; i++) value[i - 1] = kv[i];
//				postDataMap.put(kv[0], value);
//			}
//		});
//		return postDataMap;
//	}
//
//	/**
//	 *
//	 * @param url 请求的URL
//	 * @param isGetSession 本次访问，是否需要获取Session
//	 * @param postFormItemMap form中的元素
//	 * @param postFileItemMap 文件上传场景中，form中的file元素
//	 * @return Action方法返回的数据
//	 */
//	private static ResponseValue doPost(final String url, final boolean isGetSession, Map<String, String[]> postFormItemMap, Map<String, String[]> postFileItemMap) {
//		postFormItemMap = Optional.ofNullable(postFormItemMap).orElseGet(HashMap::new);
//
//		RequestBody body;
//		if(postFileItemMap==null) { // 普通的POST访问
//			FormBody.Builder builder = new FormBody.Builder();
//			postFormItemMap.forEach((k, v) -> {
//				if (v != null) for (String val : v) builder.add(k, val);
//			});
//			body = builder.build();
//		} else {
//			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//			postFormItemMap.forEach((k, v) -> {
//				if (v != null) for (String val : v) builder.addFormDataPart(k, val);
//			});
//			postFileItemMap.forEach((k, uploadFiles) -> {
//				for (String uploadFile : uploadFiles) {
//					RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), new File(uploadFile));
//					int loc = uploadFile.lastIndexOf(FileUtil.PATH_SEPARATOR_CHAR);
//					if(loc>-1) uploadFile = uploadFile.substring(loc+1); // 提取文件名
//					builder.addFormDataPart(k, CodecUtil.encodeURL(uploadFile), fileBody);
//				}
//			});
//			body = builder.build();
//		}
//
//		Request.Builder reqBuilder = new Request.Builder()
//				.url(url)
//				.post(body);
//		String curSessionid = _JSESSIONID_STRING.get();
//		if(StringUtil.isNotEmpty(curSessionid)) {
//			logger.trace("tid={} | request with session url:[{}] sessionid={}", Thread.currentThread().getId(), url, curSessionid);
//			reqBuilder = reqBuilder.addHeader("cookie", curSessionid);
//		}
//		Request request = reqBuilder.build();
//
//		try {
//			Response response = httpClient.newCall(request).execute();
//			if(isGetSession) {
//				if(response.isSuccessful()) {
//					Headers headers = response.headers();
//					logger.trace("tid={} | request for Session url:[{}] Header :\n{}", Thread.currentThread().getId(), url, headers);
//					List<String> cookies = headers.values("Set-Cookie");
//					for (String cookieStr : cookies) {
//						if (cookieStr.startsWith("JSESSIONID=")) {
//							int loc = cookieStr.indexOf(';');
//							if (loc > 11) {
//								String coSessionid = cookieStr.substring(0, loc);
//								if(logger.isTraceEnabled()) {
//									logger.trace("tid={} | request for Session url:[{}] Header :\n{}", Thread.currentThread().getId(), url, headers);
//								}
//								_JSESSIONID_STRING.set(cookieStr.substring(0, loc));
//								break;
//							}
//						}
//					}
//				}
//			}
//			int responseCode = response.code();
//
//			ResponseBody resBody = response.body();
//			String bodyString;
//			if(resBody==null) {
//				logger.error("url [ {} ] response body is null!", url);
//				bodyString = StringUtil.EMPTY;
//			} else {
//				bodyString = resBody.string();
//			}
//			return new ResponseValue(responseCode, bodyString);
//		} catch (IOException e) {
//			logger.error("url [ " + url + " ] execute() failed! ", e);
//			return new ResponseValue(-1, null);
//		}
//	}

	public static String getSessionid() {
		return _JSESSIONID_STRING.get();
	}

	public static List<Cookie> getCookie(String host){
		return cookieStore.get(host);
	}

	public static class ResponseValue {
		private final int code;
		private final String bodyString;
		public ResponseValue(int code, String bodyString) {
			this.code       = code;
			this.bodyString = bodyString;
		}

		public int getCode() {
			return code;
		}

		public String getBodyString() {
			return bodyString;
		}
	}
}
