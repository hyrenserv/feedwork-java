package fd.ng.test.junit;

import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.web.action.ActionResult;
import fd.ng.web.action.ActionResultHelper;
import okhttp3.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * 各项目自己编写自己的测试基础类
 * 并且使用 netclient 中的 HttpClient。
 */
@Deprecated
public abstract class OldWebBaseTestCase extends FdBaseTestCase {
	private static final MediaType _CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

	// 存储服务器返回的Cookie信息，以便提交数据时能够带着Cookie。
	protected static final ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

	// 存储服务器返回的Session信息，以便提交数据时能够带着Session。
	// 因为Junit是对每个方法重建一个对象，所以成员变量不会在多个方法间共享
	// 所以，login作用的测试方法要首先执行，从而得到了_JSESSIONID_STRING，后面再执行具体的测试方法时，也就拿到了这个值，并addHeader再提交
	protected String _JSESSIONID_STRING=StringUtil.EMPTY;

	protected static OkHttpClient httpClient;

	@BeforeClass
	public static void start() {
		httpClient = new OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.connectTimeout(250, TimeUnit.SECONDS)   // 连接超时，设置这么大是为了断点跟踪时有足够的思考时间
				.readTimeout(250, TimeUnit.SECONDS)      // 读取超时
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
				})
				.build();
	}
	@AfterClass
	public static void end() {
		cookieStore.forEach((name, cookies)->{
			TestCaseLog.println("name=%s, cookies=%n%s %n", name,
						cookies.stream()
								.map(Cookie::toString)
								.collect(Collectors.joining("\n  ","[ "," ]"))
			);
		});

	}

	protected String post(String url) {
		return doPost(url, null, null);
	}
	/**
	 *
	 * @param url 请求的URL
	 * @param postFormItems 提交的数据。每行是一个名值组合，第一个是名字，后面的是值，可以有多个
	 * @return 返回 Action 方法的返回数据
	 */
	protected String post(String url, String[][] postFormItems){
		return doPost(url, genPostDataMap(postFormItems), null);
	}
	protected String post(String url, String[][] postFormItems, String[][] postFileItems){
		return doPost(url, genPostDataMap(postFormItems), genPostDataMap(postFileItems));
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
	protected String post(String url, Map<String, String[]> postFormItemMap) {
		return doPost(url, postFormItemMap, null);
	}
	protected String post(String url, Map<String, String[]> postFormItemMap, Map<String, String[]> postFileItemMap) {
		return doPost(url, postFormItemMap, postFileItemMap);
	}

	/**
	 *
	 * @param url 请求的URL
	 * @param postFormItemMap form中的元素
	 * @param postFileItemMap 文件上传场景中，form中的file元素
	 * @return Action方法返回的数据
	 */
	private String doPost(final String url, Map<String, String[]> postFormItemMap, Map<String, String[]> postFileItemMap) {
		postFormItemMap = Optional.ofNullable(postFormItemMap).orElseGet(HashMap::new);

		RequestBody body;
		if(postFileItemMap==null) {
			FormBody.Builder builder = new FormBody.Builder();
			postFormItemMap.forEach((k, v) -> {
				if (v != null) for (String val : v) builder.add(k, val);
			});
			body = builder.build();
		} else {
			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			postFormItemMap.forEach((k, v) -> {
				if (v != null) for (String val : v) builder.addFormDataPart(k, val);
			});
			postFileItemMap.forEach((k, uploadFiles) -> {
				for (String uploadFile : uploadFiles) {
					RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), new File(uploadFile));
					int loc = uploadFile.lastIndexOf(FileUtil.PATH_SEPARATOR_CHAR);
					if(loc>-1) uploadFile = uploadFile.substring(loc+1); // 提取文件名
					builder.addFormDataPart(k, CodecUtil.encodeURL(uploadFile), fileBody);
				}
			});
			body = builder.build();
		}

		Request.Builder reqBuilder = new Request.Builder()
				.url(url)
				.post(body);
		if(!StringUtil.EMPTY.equals(_JSESSIONID_STRING)) {
//			println("add JSESSIONID("+_JSESSIONID_STRING+") to header");
			reqBuilder = reqBuilder.addHeader("cookie", _JSESSIONID_STRING);
		}
		Request request = reqBuilder.build();

		Response response = null;
		try {
			response = httpClient.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace(System.out);
			fail("url [ " + url + " ] execute() failed! ");
		}
		int responseCode = response.code();
		assertThat("HTTP 访问异常！responseCode="+responseCode+", url=" + url, responseCode, is(200));

		// 获取服务器返回的数据
		String responseValue = null;
		try {
			responseValue = Optional.ofNullable(response.body())
					.orElseThrow(()->new RuntimeException(url + " post response body null!"))
					.string().trim();
		} catch (IOException e) {
			fail("url [ " + url + " ] body().string() failed! " + e.toString());
		}
//		assertThat("处理失败：" + ar.getMessage() + " url=" + url, ar.isSuccess(), is(true));

		// 处理 SESSION 信息。没有下面的if，则有可能把上次的sessionid更新掉？？
//		if(StringUtil.EMPTY.equals(_JSESSIONID_STRING))
		{
			Headers headers = response.headers();
//			println("url = " + url);
//			println("headers = [" + headers + "]");
			List<String> cookies = headers.values("Set-Cookie");
			for (String cookieStr : cookies) {
				if (cookieStr.startsWith("JSESSIONID=")) {
					int loc = cookieStr.indexOf(';');
					if (loc > 11) {
						_JSESSIONID_STRING = cookieStr.substring(0, loc);
//						println("get session : " + _JSESSIONID_STRING);
					}
				}
			}
		}

		ActionResult ar = ActionResultHelper.fromJson(responseValue);
		if(!ar.isSuccess()) {
			System.out.printf("%n====> Error Action Processing! url = %s %nresponseValue : %s %n", url, responseValue);
		}
		return responseValue;
//		return JsonUtil.getNodeValue(responseValue, "data");
	}

	// 本函数未完成，不可用
	public String postJson(String url, String json) {
		RequestBody body = RequestBody.create(_CONTENT_TYPE_JSON, json);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		try {
			Response response = httpClient.newCall(request).execute();
			int responseCode = response.code();
			assertThat("HTTP 访问异常！url=" + url, responseCode, is(200));
			String responseValue = Optional.ofNullable(response.body())
					.orElseThrow(()->new RuntimeException(url + " postJson response body null!"))
					.string().trim();
			ActionResult ar = ActionResultHelper.fromJson(responseValue);
			assertThat("处理失败：" + ar.getMessage() + " url=" + url, ar.isSuccess(), is(true));

			return JsonUtil.getNodeValue(responseValue, "data");
		} catch (IOException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException("url [ " + url + " ] post failed! " + e.toString());
		}
	}
}
