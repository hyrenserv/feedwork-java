package fd.ng.db.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.yaml.*;
import fd.ng.db.jdbc.DefaultDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.util.*;

/**
 */
public class DbinfosConf {
	private static final Logger logger = LogManager.getLogger(DbinfosConf.class.getName());
	public static final String DEFAULT_DBNAME = "default";
	public static final DbinfosConf.Global globalConf; // 存储全局配置信息
	public static final Map<String, Dbinfo> Dbinfos; // 存储每一个配置的数据库连接属性。key为每组的name值
	public static final Map<String, DataSource> DATA_SOURCE;
	public static final List<String> dbNames; // 唯一的作用就是避免从Databases的keySet取所有名字时无法提前知道总数。

	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("dbinfo")).asMap();

		// 加载全局属性
		globalConf = new DbinfosConf.Global();
		YamlMap global = rootConfig.getMap("global");
		if(global!=null) {
			if(global.exist("fetch_size")) globalConf.setFetch_size(global.getInt("fetch_size"));
			if(global.exist("max_rows")) globalConf.setMax_rows(global.getInt("max_rows"));
			if(global.exist("fetch_direction")) globalConf.setFetch_direction(global.getInt("fetch_direction"));
			if(global.exist("query_timeout")) globalConf.setQuery_timeout(global.getInt("query_timeout"));

			if(global.exist("max_result_rows")) globalConf.setMax_result_rows(global.getInt("max_result_rows"));
			if(global.exist("show_sql")) globalConf.setShow_sql(global.getBool("show_sql"));
			if(global.exist("show_conn_time")) globalConf.setShow_conn_time(global.getBool("show_conn_time"));
			if(global.exist("show_sql_time")) globalConf.setShow_sql_time(global.getBool("show_sql_time"));
			if(global.exist("longtime_sql")) globalConf.setLongtime_sql(global.getLong("longtime_sql"));
		}

		// 加载每个数据的属性
		YamlArray databases = rootConfig.getArray("databases");
		if(databases==null) {
			// 出现这个异常，一般是开发环境中改了需要这个文件的程序，导致无法静态加载。因为静态加载是编译阶段。
			// 所以，最简单的方式就是全部重新编译整个工程即可
			throw new FrameworkRuntimeException("\nCan not found dbinfo.conf, Or, missing key [ databases ] in dbinfo.conf!" +
					"\nFor fix this problem:" +
					"\nIn dev        time: Rebuild whole project" +
					"\nIn production time: Restart yours application");
		}
		Map<String, Dbinfo> __Databases = new HashMap<>(databases.size());
		Map<String, DataSource> __DATA_SOURCE = new HashMap<>(databases.size());
		List<String> dbNameList = new ArrayList<>(databases.size());
		for(int i=0; i<databases.size(); i++) {
			YamlMap dbconf = databases.getMap(i);
			if(dbconf.getBool("disable", false)) continue;

			Dbinfo dbconfObject = new Dbinfo();

			String name = dbconf.getString("name");
			if(__Databases.containsKey(name)) throw new FrameworkRuntimeException("databases : name="+name+" already exists !");
			dbconfObject.setName(name);

			Dbtype dbtype = dbconf.getEnum(Dbtype.class, "dbtype");
			dbconfObject.setDbtype(dbtype);

			dbconfObject.setDriver(dbconf.getString("driver"));
			dbconfObject.setUrl(dbconf.getString("url"));
			dbconfObject.setUsername(dbconf.getString("username"));
			dbconfObject.setPassword(dbconf.getString("password"));

			dbconfObject.setAutoCommit(dbconf.getBool("autoCommit", true));

			ConnWay way = dbconf.getEnum(ConnWay.class, "way");
			dbconfObject.setWay(way);
			if(way==ConnWay.POOL) {
				// 池大小参数
				int minPoolSize = dbconf.getInt("minPoolSize", 5);
				int maxPoolSize = dbconf.getInt("maxPoolSize", 5);
				if(DEFAULT_DBNAME.equals(name)) { // 如果是默认连接，并且没有设置最大或最小，那么分别设置为10和20
					if(!dbconf.exist("minPoolSize")) minPoolSize = 10;
					if(!dbconf.exist("maxPoolSize")) minPoolSize = 20;
				}
				dbconfObject.setMinPoolSize(minPoolSize);
				dbconfObject.setMaxPoolSize(maxPoolSize);

				if(dbconf.exist("properties")) {
					YamlArray properties = dbconf.getArray("properties");
					for(int j=0; j<properties.size(); j++) {
						String line = properties.getString(j);
						YamlMapAnywhere oneProp = (YamlMapAnywhere)YamlFactory.getYamlReader(line).asMap();
						for(final YamlNode key : oneProp.keys()) {
							String keyName = ((Scalar)key).value();
							String val = oneProp.getString(keyName);
							dbconfObject.addProperty(keyName, val);
						}
					}
				}

				__DATA_SOURCE.put(name, DefaultDataSource.getDataSource( dbconfObject));
			}

			// 设置通用属性
			if(dbconf.exist("fetch_size")) dbconfObject.setFetch_size(dbconf.getInt("fetch_size"));
			else dbconfObject.setFetch_size(globalConf.getFetch_size());
			if(dbconf.exist("max_rows")) dbconfObject.setMax_rows(dbconf.getInt("max_rows"));
			else dbconfObject.setMax_rows(globalConf.getMax_rows());
			if(dbconf.exist("fetch_direction")) dbconfObject.setFetch_direction(dbconf.getInt("fetch_direction"));
			else dbconfObject.setFetch_direction(globalConf.getFetch_direction());
			if(dbconf.exist("query_timeout")) dbconfObject.setQuery_timeout(dbconf.getInt("query_timeout"));
			else dbconfObject.setQuery_timeout(globalConf.getQuery_timeout());

			if(dbconf.exist("max_result_rows")) dbconfObject.setMax_result_rows(dbconf.getInt("max_result_rows"));
			else dbconfObject.setMax_result_rows(globalConf.getMax_result_rows());
			if(dbconf.exist("show_sql")) dbconfObject.setShow_sql(dbconf.getBool("show_sql"));
			else dbconfObject.setShow_sql(globalConf.show_sql());
			if(dbconf.exist("show_conn_time")) dbconfObject.setShow_conn_time(dbconf.getBool("show_conn_time"));
			else dbconfObject.setShow_conn_time(globalConf.isShow_conn_time());
			if(dbconf.exist("show_sql_time")) dbconfObject.setShow_sql_time(dbconf.getBool("show_sql_time"));
			else dbconfObject.setShow_sql_time(globalConf.isShow_sql_time());
			if(dbconf.exist("longtime_sql")) dbconfObject.setLongtime_sql(dbconf.getLong("longtime_sql"));
			else dbconfObject.setLongtime_sql(globalConf.getLongtime_sql());

			/** 各个参数的相关性检查 */
//				if(dbconfObject.getFetch_size()>0&&dbconfObject.getMax_result_rows()>0) {
//					if( dbconfObject.getFetch_size()>dbconfObject.getMax_result_rows() ) {
//						logger.warn("Fix fetch_size(={}) by max_result_rows(={})", dbconfObject.getFetch_size(), dbconfObject.getMax_result_rows());
//						dbconfObject.setFetch_size(dbconfObject.getMax_result_rows());
//					}
//				}
			__Databases.put(name, dbconfObject);
			dbNameList.add(name);
		}
		if(!__Databases.containsKey(DEFAULT_DBNAME)) {
			throw new FrameworkRuntimeException("There must be one 'name : default' item in 'databases' !");
		}
		DATA_SOURCE = Collections.unmodifiableMap(__DATA_SOURCE);
		Dbinfos = Collections.unmodifiableMap(__Databases);
		dbNames = Collections.unmodifiableList(dbNameList);
	}

	public static Dbinfo getDatabase(String name) {
		if(name==null)
			return Dbinfos.get(DEFAULT_DBNAME);
		else
			return Dbinfos.get(name);
	}
	public static String string() {
		return "global : " + globalConf.toString() + "\n" +
				"databases : " + Dbinfos.toString();
	}

	public static class Global {
		/** JDBC 参数 */
		protected Integer fetch_size = null;
		protected Integer max_rows = null; // JDBC执行SQL时，获得的结果集最大允许多少条。-1为不设置这个值
		protected Integer fetch_direction = null;
		protected Integer query_timeout = null;

		protected Integer max_result_rows = null; // 不同于max_rows，这是轮询 ResultSet 构造结果数据集时，最多循环多少次
		protected boolean show_conn_time = false; // 是否显示数据连接的时间消耗
		protected boolean show_sql = true; // 是否显示sql
		protected boolean show_sql_time = false; // 是否显示sql执行时间
		protected long longtime_sql = 99999; // SQL执行时间超过这个值的，显示到日志中

		public Integer getFetch_size() {
			return fetch_size;
		}
		public boolean hasFetch_size() {
			return fetch_size != null;
		}
		public Integer getMax_rows() {
			return max_rows;
		}
		public boolean hasMax_rows() {
			return max_rows != null;
		}
		public Integer getFetch_direction() {
			return fetch_direction;
		}
		public boolean hasFetchDirection() {
			return fetch_direction != null;
		}
		public Integer getQuery_timeout() {
			return query_timeout;
		}
		public boolean hasQuery_timeout() {
			return query_timeout != null;
		}

		public void setFetch_size(Integer fetch_size) {
			this.fetch_size = fetch_size;
		}

		public void setMax_rows(Integer max_rows) {
			this.max_rows = max_rows;
		}

		public void setFetch_direction(Integer fetch_direction) {
			this.fetch_direction = fetch_direction;
		}

		public void setQuery_timeout(Integer query_timeout) {
			this.query_timeout = query_timeout;
		}

		public int getMax_result_rows() {
			return max_result_rows;
		}
		public boolean isSetMax_result_rows() {
			return max_result_rows != null;
		}
		public void setMax_result_rows(Integer max_result_rows) {
			this.max_result_rows = max_result_rows;
		}

		public boolean isShow_conn_time() {
			return show_conn_time;
		}

		public void setShow_conn_time(boolean show_conn_time) {
			this.show_conn_time = show_conn_time;
		}

		public boolean show_sql() {
			return show_sql;
		}

		public void setShow_sql(boolean show_sql) {
			this.show_sql = show_sql;
		}

		public boolean isShow_sql_time() {
			return show_sql_time;
		}

		public void setShow_sql_time(boolean show_sql_time) {
			this.show_sql_time = show_sql_time;
		}

		public long getLongtime_sql() {
			return longtime_sql;
		}

		public void setLongtime_sql(long longtime_sql) {
			this.longtime_sql = longtime_sql;
		}

		@Override
		public String toString() {
			return "{" +
					"fetch_size=" + fetch_size +
					", max_rows=" + max_rows +
					", fetch_direction=" + fetch_direction +
					", query_timeout=" + query_timeout +
					", max_result_rows=" + max_result_rows +
					", show_conn_time=" + show_conn_time +
					", show_sql=" + show_sql +
					", show_sql_time=" + show_sql_time +
					", longtime_sql=" + longtime_sql +
					'}';
		}
	}

	public static class Dbinfo extends Global {
		private String name;
		private ConnWay way = ConnWay.NONE;
		private Dbtype dbtype = Dbtype.NONE;
		private String driver;
		private String url;
		private String username;
		private String password;
		private boolean autoCommit = true;

		public boolean autoCommit() {
			return autoCommit;
		}

		public void setAutoCommit(final boolean autoCommit) {
			this.autoCommit = autoCommit;
		}

		private int minPoolSize;
		private int maxPoolSize;
		private final Map<String, String> propertyMap = new HashMap<>(0);

		public int getMinPoolSize() {
			return minPoolSize;
		}

		public void setMinPoolSize(final int minPoolSize) {
			this.minPoolSize = minPoolSize;
		}

		public int getMaxPoolSize() {
			return maxPoolSize;
		}

		public void setMaxPoolSize(final int maxPoolSize) {
			this.maxPoolSize = maxPoolSize;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ConnWay getWay() {
			return way;
		}

		public void setWay(ConnWay way) {
			this.way = way;
		}

		public Dbtype getDbtype() {
			return dbtype;
		}

		public void setDbtype(Dbtype dbtype) {
			this.dbtype = dbtype;
		}

		public String getDriver() {
			return driver;
		}

		public void setDriver(String driver) {
			this.driver = driver;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void addProperty(String key, String val) {
			propertyMap.put(key, val);
		}

		public Map<String, String> getProperties() {
			return this.propertyMap;
		}

		@Override
		public String toString() {
			String v = "Dbinfo{name='" + name + "'";
			if(logger.isTraceEnabled()) {
				v += ", way=" + way +
						", dbtype=" + dbtype +
						", fetch_size=" + (fetch_size == null ? "null" : fetch_size.toString()) +
						", max_rows=" + (max_rows == null ? "null" : max_rows.toString()) +
						", fetch_direction=" + (fetch_direction == null ? "null" : fetch_direction.toString()) +
						", query_timeout=" + (query_timeout == null ? "null" : query_timeout.toString()) +
						", max_result_rows=" + max_result_rows +
						", show_conn_time=" + show_conn_time +
						", show_sql=" + show_sql +
						", show_sql_time=" + show_sql_time +
						", longtime_sql=" + longtime_sql +
						", driver='" + driver + '\'' +
						", url='" + url + '\'' +
						", username='" + username + '\'' +
						", password='" + password + '\'';
				if (this.propertyMap.size()>0)
					v = v + ", " + this.propertyMap.toString();
			}
			v = v +	'}';
			return v;
		}
	}
}
