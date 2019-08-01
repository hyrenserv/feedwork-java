package fd.ng.web.conf;

import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;

import java.io.File;

/**
 * 本类的加载方式为： ClassUtil.loadClass(DbinfoHelper.class.getName());
 */
public final class WebinfoConf {
	public static final boolean WithDatabase;   // 这个webserver是否有DB处理。目前没使用
//	public static final boolean ShowLog;
	public static final int     ActionLongtime;

	// cookie
	public static final int     Cookie_MaxAge; // cookie过期时间，单位是秒，默认为8小时
	public static final boolean Cookie_HttpOnly;  // true：通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。
	public static final String  Cookie_Path; // 默认不设置
	// 当设置为true时，表示创建的 Cookie 会被以安全的形式向服务器传输.
	// 也就是只能在 HTTPS 连接中被浏览器传递到服务器端进行会话验证，如果是 HTTP 连接则不会传递该信息，所以不会被窃取到Cookie 的具体内容。
	public static final boolean Cookie_Secure;

	// CORS 跨域配置
	public static final boolean CORS_Allow;
	public static final String  CORS_acao;  // Access-Control-Allow-Origin
	public static final String  CORS_acam;  // Access-Control-Allow-Methods
	public static final String  CORS_acac;  // Access-Control-Allow-Credentials

	// 文件上传的配置数据
	public static final int     FileUpload_SizeThreshold;
	public static final String  FileUpload_Repository;
	public static final File    FileUpload_RepositoryDir;
	public static final int     FileUpload_FilesTotalSize;
	public static final String  FileUpload_SavedDirName; // 用户设置的，用于保存上传文件的目录

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("webinfo")).asMap();

		WithDatabase = rootConfig.getBool("with.database", true);
//		ShowLog = rootConfig.getBool("showlog", true);
		ActionLongtime = rootConfig.getInt("action.longtime", -1);

		// cookie
		YamlMap cookie = rootConfig.getMap("session");
		if(cookie!=null) {
			Cookie_MaxAge   = cookie.getInt("maxage", 28800); // 8考试
			Cookie_HttpOnly = cookie.getBool("httponly", false);
			Cookie_Path     = cookie.getString("path", null);
			Cookie_Secure   = cookie.getBool("secure", false);
		} else {
			Cookie_MaxAge   = 28800; // 8考试
			Cookie_HttpOnly = false;
			Cookie_Path     = null;
			Cookie_Secure   = false;
		}

		// 跨域
		YamlMap cors = rootConfig.getMap("cors");
		if(cors!=null) {
			CORS_Allow = cors.getBool("cors.allow", false);
			CORS_acao  = cors.getString("cors.acao", "null");
			CORS_acam  = cors.getString("cors.acam", "POST, GET");
			CORS_acac  = cors.getString("cors.acac", "true");
		} else {
			CORS_Allow = false;
			CORS_acao  = "null";
			CORS_acam  = "POST, GET";
			CORS_acac  = "true";
		}

		// 文件上传参数
		YamlMap fileupload = rootConfig.getMap("fileupload");
		if(fileupload!=null) {
			FileUpload_SizeThreshold = fileupload.getInt("fileupload.SizeThreshold", 5 * 1024 * 1024); // 3M
			FileUpload_FilesTotalSize = fileupload.getInt("fileupload.FilesTotalSize", 5 * 1024 * 1024);
		} else {
			FileUpload_SizeThreshold = 5 * 1024 * 1024;
			FileUpload_FilesTotalSize = 5 * 1024 * 1024;
		}
		// 超内存限制后的临时保存目录
		FileUpload_Repository = getDirString(fileupload, "fileupload.Repository");
		FileUpload_RepositoryDir = new File(FileUpload_Repository);
		if (!FileUpload_RepositoryDir.exists() || !FileUpload_RepositoryDir.isDirectory())
			throw new FrameworkRuntimeException("fileupload.Repository wrong, must be Dir.");

		// 用户预期要保存上传文件的目录
		FileUpload_SavedDirName = getDirString(fileupload, "fileupload.SavedDir");
	}

	private WebinfoConf() { throw new AssertionError("No WebinfoConf instances for you!"); }

	private static String getDirString(YamlMap config, String key) {
		if(config==null) return FileUtil.TEMP_DIR_NAME;
		if(config.exist(key)) { // 配置了明确的属性
			String tmpTempSavedDir = config.getString(key);
			if(tmpTempSavedDir.charAt(tmpTempSavedDir.length()-1) == FileUtil.PATH_SEPARATOR_CHAR) return tmpTempSavedDir;
			else return tmpTempSavedDir+FileUtil.PATH_SEPARATOR_CHAR;
		} else {
			return FileUtil.TEMP_DIR_NAME;
		}
	}

	public static String string() {
		return "Webinfo{" +
				"action.longtime=" + ActionLongtime +

				", fileupload.SavedDir=" + FileUpload_SavedDirName +

				", Cookie_MaxAge=" + Cookie_MaxAge +
				", Cookie_HttpOnly=" + Cookie_HttpOnly +
				", Cookie_Path=" + Cookie_Path +
				", Cookie_Secure=" + Cookie_Secure +

				", cors.allow=" + CORS_Allow +
				", cors.acao=" + CORS_acao +
				", cors.acam=" + CORS_acam +
				", cors.acac=" + CORS_acac +
				'}';
	}
}
