package fd.ng.web.helper;

import fd.ng.core.utils.UuidUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.web.conf.WebinfoConf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpDataHolder {
	/**
	 * 每次访问，当前线程拥有的数据
	 */
	private static final ThreadLocal<HttpDataHolder> _httpdataBox = new ThreadLocal<>();
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final String bizid;
	private final String actionPathName;
	private final String actionMethodName;
	private final Map<String, DatabaseWrapper> dbBox;

	private HttpDataHolder(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;

		String pathInfo = request.getPathInfo();
		int lastLoc = pathInfo.lastIndexOf('/');
		this.actionPathName = pathInfo.substring( 0, lastLoc);
		this.actionMethodName = pathInfo.substring( lastLoc+1 );
		StringBuilder sb = new StringBuilder(32);
		sb.append('<').append(this.actionMethodName).append(':').append(Thread.currentThread().getId())
				.append(':').append(UuidUtil.elapsedNanoTime()).append('>');
		this.bizid = sb.toString();

//		// 创建DB对象。对于JDBC方式不会建立连接
		int size = DbinfosConf.dbNames.size();
		dbBox = new HashMap<>(size);
		for (int i = 0; i < size; i++) {
			String dbname = DbinfosConf.dbNames.get(i);
			dbBox.put(dbname, new DatabaseWrapper.Builder()
					.dbname(dbname).lazyConnect(true).id(this.bizid)
					.create());
		}
	}
	/**
	 * 初始化
	 */
	public static void init(HttpServletRequest request, HttpServletResponse response) {
		_httpdataBox.set(new HttpDataHolder(request, response));
	}
	public static void relase() {
		Set<Map.Entry<String, DatabaseWrapper>> dbEntries = _httpdataBox.get().dbBox.entrySet();
		for (Map.Entry<String, DatabaseWrapper> entry : dbEntries) {
			entry.getValue().close();
		}
		_httpdataBox.remove();
	}

	public static String getBizId() { return _httpdataBox.get().bizid; }
	public static String getActionPathName() { return _httpdataBox.get().actionPathName; }
	public static String getActionMethodName() { return _httpdataBox.get().actionMethodName; }

	public static Collection<DatabaseWrapper> takeoutAllDB() {
		return _httpdataBox.get().dbBox.values();
	}
	/**
	 * 获取一个DB，并且建立好jdbc连接
	 * @return
	 */
	public static DatabaseWrapper getOrConnectDB() {
		return getOrConnectDB(DbinfosConf.DEFAULT_DBNAME);
	}
	/**
	 * 获取一个DB，并且建立好jdbc连接
	 * @return
	 */
	public static DatabaseWrapper getOrConnectDB(String dbname) {
		DatabaseWrapper db = _httpdataBox.get().dbBox.get(dbname);
		if(db==null) return null;
		else return db.makeConnection();
	}

	public static HttpServletRequest getRequest() {
		return _httpdataBox.get().request;
	}

	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	public static HttpServletResponse getResponse() {
		return _httpdataBox.get().response;
	}
}
