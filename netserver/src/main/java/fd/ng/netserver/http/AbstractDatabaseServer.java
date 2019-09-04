package fd.ng.netserver.http;

import fd.ng.core.utils.ClassUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.netserver.conf.HttpServerConfBean;

public abstract class AbstractDatabaseServer extends AbstractBaseServer {
	public AbstractDatabaseServer(HttpServerConfBean confBean) { super(confBean); }
	public AbstractDatabaseServer(String serverName,HttpServerConfBean confBean) { super(serverName,confBean);}
	@Override
	protected void doInit() throws Exception {
		ClassUtil.loadClass(DbinfosConf.class.getName());
		logger.info("Initialize dbinfo  done.");
	}
}
