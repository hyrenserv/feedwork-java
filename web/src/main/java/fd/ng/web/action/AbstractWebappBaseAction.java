package fd.ng.web.action;

import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.web.helper.HttpDataHolder;
import fd.ng.web.util.Dbo;
import fd.ng.web.conf.WebinfoConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WEB项目的基类。
 */
public abstract class AbstractWebappBaseAction extends AbstractBaseAction {
	private static final Logger logger = LogManager.getLogger(AbstractWebappBaseAction.class.getName());

	/**
	 * 项目中覆盖本方法，处理登陆session等功能。
	 * @param request HttpServletRequest
	 * @return 一般返回null
	 * 如果需要终止action方法的执行，则返回ActionResult对象，且code不能设置为success码
	 * 例如： return ActionResultHelper.bizError("no login");
	 */
	@Override
	protected ActionResult _doPreProcess(HttpServletRequest request) {
		return null;
	}

	/**
	 * 自动提交事务，并关闭数据库连接。
	 * @param request HttpServletRequest
	 * @return 一般返回null
	 * 如果需要返回数据给前端（也即：屏蔽action方法的返回值），则返回ActionResult对象，且code不能设置为success码
	 */
	@Override
	protected ActionResult _doPostProcess(HttpServletRequest request) {
		/**
		 * 自动提交事务。
		 * 如果整个项目都需要对事务的细粒度控制，那么可以在项目的BaseAction中重载本方法。
		 * 如果某个Action需要对事务的细粒度控制，那么该Action可以重载本方法。
		 */
		HttpDataHolder.takeoutAllDB().forEach(db->{
			db.commit();
			if(logger.isTraceEnabled()) logger.trace("db:{} auto commit by _doPostProcess", db.getID());
		});
//		if(WebinfoConf.WithDatabase) {
//			Map<String, DatabaseWrapper> dbBox = Dbo._NO_USED0();
//			dbBox.forEach((name, db)->{
//				db.commit();
//				if(logger.isTraceEnabled()) logger.trace("db:{} auto commit by _doPostProcess", db.getID());
//			});
//		}
		return null;
	}

	/**
	 * 发生异常时，自动回滚事务。
	 * 如果子类要重载本方法，可以完成自己的处理后，调用 super._doExceptionProcess
	 * @param request HttpServletRequest
	 */
	@Override
	protected void _doExceptionProcess(HttpServletRequest request) {
		HttpDataHolder.takeoutAllDB().forEach(db->{
			db.rollback();
			if(logger.isTraceEnabled()) logger.trace("db:{} auto rollback by _doExceptionProcess", db.getID());
		});
//		if(WebinfoConf.WithDatabase) {
//			Map<String, DatabaseWrapper> dbBox = Dbo._NO_USED0();
//			dbBox.forEach((name, db)->{
//				db.rollback();
//				if(logger.isTraceEnabled()) logger.trace("db:{} auto rollback by _doExceptionProcess", db.getID());
//			});
//		}
	}

//	protected DatabaseWrapper getDB() {
//		DatabaseWrapper db = HttpDataHolder.getDB();
//		return db;
//	}
//	protected DatabaseWrapper getDB(String dbName) {
//		DatabaseWrapper db = HttpDataHolder.getDB(dbName);
//		return db;
//	}
//
//	/**
//	 * 把DB ID与当前访问的URL关联打印出来，用于调试或排查问题
//	 */
//	private void logInfoTrace(HttpServletRequest request) {
//		if(logger.isInfoEnabled()) {
//			try {
//				StringJoiner sj = new StringJoiner(", ");
//				Optional.ofNullable(SqlOperator._NO_USED0())
//						.ifPresent(dbBox -> dbBox.forEach((k, v) -> sj.add(v.getID())));
//				if(sj.length()>0)
//					logger.info(Loghelper.fitMessage("connctions is [%s]", sj.toString()));
//			} catch (Exception e) {
//				logger.error(HttpDataHolder.getBizId(),e);
//			}
//		}
//	}
}
