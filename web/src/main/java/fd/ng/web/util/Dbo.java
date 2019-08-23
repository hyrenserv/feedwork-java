package fd.ng.web.util;

import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.Page;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.resultset.Result;
import fd.ng.web.helper.HttpDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 在当前线程中拿到的是同一个DB。
 * 只有在有全局总控的程序中，才能使用本类，比如 Web 应用中。
 * 因为，必须在一个线程结束前，调用 release 方法来释放资源！！！
 *
 * 如果，业务层使用了非默认连接，则其用法如下：
 * DatabaseWrapper db = Dbo.db("pgsql"）;
 * Dbo.execute(db, sql);
 * 即：先用 dbinfo 里面定义的数据库名字获取 DatabaseWrapper 对象，再调用各个操作函数
 */
public class Dbo extends SqlOperator {
	private static final Logger logger = LogManager.getLogger(Dbo.class.getName());
//	private static final ThreadLocal<Map<String, DatabaseWrapper>> _dbBoxThreadLocal = new ThreadLocal<>();
	public Dbo() { throw new AssertionError("There's no need for you to construct Dbo!"); }

	/**
	 * 使用dbinfo中的 default 配置项，获取DB连接对象 DatabaseWrapper。
	 * 在当前线程中拿到的是同一个DB。
	 * 也就是说，一个实例对象中的多个函数间互相调用，使用的是同一个DB。
	 *
	 * @return DatabaseWrapper
	 */
	public static DatabaseWrapper db() { return HttpDataHolder.getOrConnectDB(); }
	/**
	 * 使用dbinfo中 配置项，获取DB连接对象 DatabaseWrapper。
	 * 在当前线程中拿到的是同一个DB。
	 * 也就是说，一个实例对象中的多个函数间互相调用，使用的是同一个DB。
	 *
	 * @param dbname dbinfo中 databases 的 name
	 * @return DatabaseWrapper
	 */
	public static DatabaseWrapper db(String dbname) {
		return HttpDataHolder.getOrConnectDB(dbname);
	}

	// ---------- 因为在 HttpDataHolder 中处理了DB，所以，以下几个方法不再需要。放在这里仅用于给出代码参考 -----------
//	/**
//	 * 获取当前线程的DB对象，如果不存在则创建一个新DB。
//	 * 因为在 HttpDataHolder 中创建了连接，所以，这个方法不需要使用。放在这里仅用于给出代码参考
//	 *
//	 * @param dbBuilder DatabaseWrapper.Builder
//	 * @return DatabaseWrapper
//	 */
//	private static DatabaseWrapper createOrAttainDB(DatabaseWrapper.Builder dbBuilder) {
//		if(logger.isTraceEnabled()) logger.trace("create or attain db in thread.id={}", Thread.currentThread().getId());
//		String dbname = dbBuilder.getDbname();
//		Map<String, DatabaseWrapper> dbBox = _dbBoxThreadLocal.get();
//		if(dbBox==null) { // 本线程第一次获取数据库连接
//			DatabaseWrapper db = dbBuilder.create();
//			dbBox = new HashMap<>();
//			dbBox.put(db.getName(), db);
//			_dbBoxThreadLocal.set(dbBox);
//			if(logger.isTraceEnabled()) logger.trace("Make new thread resource pool(hashCode={}), in thread id={}", dbBox.hashCode(), Thread.currentThread().getId());
//			return db;
//		} else {
//			DatabaseWrapper db = dbBox.get(dbname);
//			if(db==null) { // 使用当前名字没有建立过 DatabaseWrapper
//				db = dbBuilder.create();
//				dbBox.put(dbname, db);
//			}
//			return db;
//		}
//	}
//
//	/**
//	 * 取得当前在用的DB对象。
//	 * 如果名字不对，则返回为null。
//	 * 无特殊原因，不建议使用本函数！！！
//	 * @return DatabaseWrapper
//	 */
//	public static DatabaseWrapper attainCurrentDB() { return attainCurrentDB(null); }
//	public static DatabaseWrapper attainCurrentDB(String dbname) {
//		Map<String, DatabaseWrapper> dbBox = _dbBoxThreadLocal.get();
//		if(dbBox==null) return null;
//		return dbBox.get( Optional.ofNullable(dbname).orElse(DbinfosConf.DEFAULT_DBNAME) );
//	}
//
//	/**
//	 * 该方法不允许任意使用！
//	 * @return Object There's nothing to tell
//	 */
//	public static Map<String, DatabaseWrapper> _NO_USED0() {
//		// 获得当前线程生命周期中所有被建立的DB对象。
//		return _dbBoxThreadLocal.get();
//	}
//	/**
//	 * 该方法不允许任意使用！
//	 * @return Object There's nothing to tell
//	 */
//	public static void _NO_USED1() {
//		/**
//		 * 回滚当前线程生命周期中的DB对象。
//		 * 在诸如 AbstractWebappBaseAction 这样的总控工具类中使用。
//		 * 如果在没有总控的场景中使用 Dbo ，可通过如下方式控制事务：
//		 * Dbo.attainDB().beginTrans();
//		 */
//		Optional.ofNullable(_dbBoxThreadLocal.get()).ifPresent(dbBox->{
//			dbBox.forEach((k, v)->v.rollback());
//			if(logger.isTraceEnabled())
//				logger.trace("Release thread resource pool : {}", dbBox.hashCode());
//		});
//		if(logger.isTraceEnabled()) {
//			Optional.ofNullable(_dbBoxThreadLocal.get()).ifPresent(dbBox->{
//				StringBuilder sbLogger = new StringBuilder("auto rollback by Dbo :");
//				dbBox.forEach((k, v)->sbLogger.append("\n  rollback db : ").append(v.toString()));
//				logger.trace(sbLogger.toString());
//			});
//		}
//	}
//
//	public static void release() {
//		Map<String, DatabaseWrapper> dbBox = _dbBoxThreadLocal.get();
//		if(dbBox==null) {
//			if(logger.isTraceEnabled()) logger.trace("Nothing release in thread.id={}", Thread.currentThread().getId());
//			return;
//		}
//		dbBox.forEach((k, v)->v.close());
//		_dbBoxThreadLocal.remove();
//		if(logger.isTraceEnabled()) {
//			logger.trace("release done in thread.id={}", Thread.currentThread().getId());
//		}
//	}

	// -------------------- 公共方法 --------------------
	/**
	 * 开启事务。不建议在业务层主动调用。
	 * 本类的 execute 方法中已经自动开启了。
	 */
	public static void beginTransaction() { SqlOperator.beginTransaction(db()); }

	/**
	 * 提交事务。对于WEB类程序，绝大多数情况下，在业务层不需要主动调用。
	 *
	 * 在业务层，除非需要在代码处理流程中阶段性的提交事务，否则无需主动调用。
	 * 对于WEB类程序，已经通过 AbstractWebappBaseAction._doPostProcess 进行了自动提交。
	 * 也就是说，只要代码处理流程中，没有发生异常或主动抛出异常，都会自动提交事务。
	 */
	public static void commitTransaction() { SqlOperator.commitTransaction(db()); }

	/**
	 * 回滚事务。对于WEB类程序，绝大多数情况下，在业务层不需要主动调用。
	 * 在业务层，根据处理流程的需要而使用，比如：
	 * 发生了某种业务上的状况（不是异常），需要回滚前面的数据，并且处理逻辑继续向下执行时，需主动调用。
	 *
	 * 【注意！注意！注意！】：
	 * 对于WEB类程序，已经通过 AbstractWebappBaseAction._doExceptionProcess 进行了自动回滚。
	 * 所以，除非需要在代码处理流程中阶段性的回滚事务，否则无需主动调用。
	 */
	public static void rollbackTransaction() { SqlOperator.rollbackTransaction(db()); }

	/**
	 * 执行增、删、改操作。
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object[] 对应sql中的'?'占位符的各个参数值
	 * @return int 该sql执行后，在DB中影响的条目数：新增的数量、修改的数量、删除的数量。
	 */
	public static int execute(String sql, Object... params) { return SqlOperator.execute(db(), sql, params); }

	/**
	 * 查询单条数据，返回JavaBean对象。
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Field<T> 泛型JavaBean对象。
	 */
	public static <T> Optional<T> queryOneObject(Class<T> classTypeOfBean, String sql, Object... params) {
		return SqlOperator.queryOneObject(db(), classTypeOfBean, sql, params);
	}

	/**
	 * 查询并返回一行数据。 慎用！
	 *
	 * 慎用！慎用！慎用！
	 *
	 * 因为，如果查询SQL返回多行数据，本函数返回的是第一行数据。
	 * 对于预期完成得到一行数据，但是又需要检查SQL是否返回了多行数据的情况，请使用 XXXListProcessor！
	 *
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Map<String, Object> 不会出现null，如果没有查询到数据，返回的是空 Map
	 */
	public static Map<String, Object> queryOneObject(String sql, Object... params) {
		return SqlOperator.queryOneObject(db(), sql, params);
	}

	/**
	 * 查询多条数据。
	 * @param classTypeOfBean Class<T> JavaBean的class类型，例如： User.class
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return List<T> 不会出现null，如果没有查询到数据，返回的是空 List
	 */
	public static <T> List<T> queryList(Class<T> classTypeOfBean, String sql, Object... params) {
		return SqlOperator.queryList(db(), classTypeOfBean, sql, params);
	}

	/**
	 * 查询多条数据。
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return List<Map<String, Object>> 不会出现null，如果没有查询到数据，返回的是空 List
	 */
	public static List<Map<String, Object>> queryList(String sql, Object... params) {
		return SqlOperator.queryList(db(), sql, params);
	}

	public static <T> List<T> queryPagedList(Class<T> classTypeOfBean, Page page, String sql, Object... params) {
		return SqlOperator.queryPagedList(db(), classTypeOfBean, page, sql, params);
	}
	public static List<Map<String, Object>> queryPagedList(Page page, String sql, Object... params) {
		return SqlOperator.queryPagedList(db(), page, sql, params);
	}

	/**
	 * 查询多条数据。
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Result 不会出现null，如果没有查询到数据，返回的是空 Result
	 */
	public static Result queryResult(String sql, Object... params) {
		return SqlOperator.queryResult(db(), sql, params);
	}

	/**
	 * 分页查询多条数据。
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Result 不会出现null，如果没有查询到数据，返回的是空 Result
	 */
	public static Result queryPagedResult(Page page, String sql, Object... params) {
		return SqlOperator.queryPagedResult(db(), page, sql, params);
	}

	/**
	 * 用于只会查询到一条数据且为一个数字的情况，比如：select count(1) from
	 * 用法：
	 * OptionalLong result = Dbo.queryNumber(......)
	 * 因为：本方法对应的SQL是类似 select count(1) from，所以必须有且只有一条数据被查询到。
	 * 所以：不满足“有且唯一”条件的就是异常。
	 * 所以：返回值使用了Optional，取值时只有下面两种方式：
	 * 1） long val = result.orElseThrow(() -> new BusinessException("错误描述"));
	 * 2） long val = result.orElse(你准备使用的默认值);
	 *
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return OptionalLong
	 */
	public static OptionalLong queryNumber(String sql, Object... params) {
		return SqlOperator.queryNumber(db(), sql, params);
	}

	/**
	 * 批量执行增、删、改操作。
	 *
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params List<Object[]> 每行是一组对应'?'占位符的数值。
	 *               如果数据很多，比如100万行，构造这么大的List担心OOM，可以分批调用本函数。比如每1万行调用一次。
	 * @return int数组的每一个值代表相应行的数据更新结果
	 */
	public static int[] executeBatch(String sql, List<Object[]> params) { return SqlOperator.executeBatch(db(), sql, params); }
}
