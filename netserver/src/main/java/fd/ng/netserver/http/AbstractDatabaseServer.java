package fd.ng.netserver.http;

import fd.ng.core.utils.ClassUtil;
import fd.ng.db.conf.DbinfosConf;

public abstract class AbstractDatabaseServer extends AbstractBaseServer {
	public AbstractDatabaseServer() { super(); }
	public AbstractDatabaseServer(String serverName) { super(serverName); }
	@Override
	protected void doInit() throws Exception {
		ClassUtil.loadClass(DbinfosConf.class.getName());
		logger.info("Initialize dbinfo  done.");
	}
}
