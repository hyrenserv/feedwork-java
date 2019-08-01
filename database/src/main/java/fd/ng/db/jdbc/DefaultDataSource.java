package fd.ng.db.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.db.conf.DbinfosConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;

public class DefaultDataSource {
	private static final Logger logger = LogManager.getLogger(DefaultDataSource.class.getName());

	public static DataSource getDataSource() {
		String dbname = null;
		return getDataSource(dbname);
	}

	public static DataSource getDataSource(String dbname) {
		DbinfosConf.Dbinfo dbconf = DbinfosConf.getDatabase(dbname);
		return getDataSource(dbconf);
	}

	public static DataSource getDataSource(DbinfosConf.Dbinfo dbinfo) {
		try {
			// 以下的 HikariConfig 对象，可以构造成静态变量
			// 因为现在是在 DbinfosConf 总静态加载，所以就这样写了
			HikariConfig config = new HikariConfig();
			config.setDriverClassName(dbinfo.getDriver());
			config.setJdbcUrl(dbinfo.getUrl());
			config.setUsername(dbinfo.getUsername());
			config.setPassword(dbinfo.getPassword());
			config.setMinimumIdle(dbinfo.getMinPoolSize());
			config.setMaximumPoolSize(dbinfo.getMaxPoolSize());

			dbinfo.getProperties().forEach(config::addDataSourceProperty);

			DataSource ds = new HikariDataSource(config);
			return ds;
		} catch (Exception e) {
			if(dbinfo==null)
				throw new RawlayerRuntimeException("Argument 'dbconf' must not null!", e);
			else
				throw new RawlayerRuntimeException("创建数据连接失败(name=" + dbinfo.getName() + ")！" +
						"如果这个连接配置不需要使用，请设置为 'disable' ！", e);
		}
	}
}
