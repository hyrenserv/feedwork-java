package fd.ng.db.jdbc;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RevitalizedCheckedException;
import fd.ng.db.resultset.Result;
import fd.ng.db.resultset.processor.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class SqlOperator {
	private static final Logger logger = LogManager.getLogger(SqlOperator.class.getName());
	public SqlOperator() { logger.error("There's no need for you to construct SqlOperator!"); }

	/**
	 * 开启事务。不建议在业务层主动调用。
	 * 本类的 execute 方法中已经自动开启了。
	 */
	public static void beginTransaction(DatabaseWrapper db) { db.beginTrans(); }
	/**
	 * 提交事务。对于WEB类程序，绝大多数情况下，在业务层不需要主动调用。
	 *
	 * 在业务层，除非需要在代码处理流程中阶段性的提交事务，否则无需主动调用。
	 * 对于WEB类程序，已经通过 AbstractWebappBaseAction._doPostProcess 进行了自动提交。
	 * 也就是说，只要代码处理流程中，没有发生异常或主动抛出异常，都会自动提交事务。
	 */
	public static void commitTransaction(DatabaseWrapper db) { db.commit(); }
	/**
	 * 回滚事务。对于WEB类程序，绝大多数情况下，在业务层不需要主动调用。
	 * 在业务层，根据处理流程的需要而使用，比如：
	 * 发生了某种业务上的状况（不是异常），需要回滚前面的数据，并且处理逻辑继续向下执行时，需主动调用。
	 *
	 * 【注意！注意！注意！】：
	 * 对于WEB类程序，已经通过 AbstractWebappBaseAction._doExceptionProcess 进行了自动回滚。
	 * 所以，除非需要在代码处理流程中阶段性的回滚事务，否则无需主动调用。
	 */
	public static void rollbackTransaction(DatabaseWrapper db) { db.rollback(); }

	/**
	 * 执行增、删、改操作。
	 * 配置了多个DB连接的情况下，需要使用非 default 连接时，使用本函数。
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return int 该sql执行后，在DB中影响的条目数：新增的数量、修改的数量、删除的数量。
	 */
	public static int execute(DatabaseWrapper db, String sql, Object... params) {
		db.beginTrans();
		return db.execute(sql, params);
	}

	/**
	 * 查询并返回一行数据。
	 *
	 * @param db DatabaseWrapper
	 * @param classOfBean Class<T> JavaBean的class类型，例如： User.class
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Optional<T> 泛型JavaBean对象。不会出现null，使用isPresent判断是否查询到了数据，或者使用orElse等方法取值
	 * @throws fd.ng.db.resultset.TooManyRecordsException 假如查询结果多余一行
	 */
	public static <T> Optional<T> queryOneObject(DatabaseWrapper db, Class<T> classOfBean, String sql, Object... params) {
		T result = db.query(sql, new BeanProcessor<>(classOfBean), params);
		return Optional.ofNullable(result);
	}

	/**
	 * 查询并返回一行数据。
	 * 没有查询到数据，返回空Map
	 *
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Map<String, Object> 不会出现null，如果没有查询到数据，返回的是空 Map
	 * @throws fd.ng.db.resultset.TooManyRecordsException 假如查询结果多余一行
	 */
	public static Map<String, Object> queryOneObject(DatabaseWrapper db, String sql, Object... params) {
		return db.query(sql, new MapProcessor(), params);
	}

	/**
	 * 查询多条数据。
	 * @param db DatabaseWrapper
	 * @param classOfBean Class<T> JavaBean的class类型，例如： User.class
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return List<T> 不会出现null，如果没有查询到数据，返回的是空 List
	 */
	public static <T> List<T> queryList(DatabaseWrapper db, Class<T> classOfBean,
	                                        String sql, Object... params) {
		return db.query(sql, new BeanListProcessor<>(classOfBean), params);
	}

	public static <T> List<T> queryPagedList(DatabaseWrapper db, Class<T> classOfBean, Page page,
	                                             String sql, Object... params) {
		if(page.getBeginOfPage()<0) throw new RevitalizedCheckedException("begin不能为负数！");
		if(page.getBeginOfPage()>=page.getEndOfPage()) throw new RevitalizedCheckedException("begin必须小于end！");
		int begin = page.getBeginOfPage();
		int end = page.getEndOfPage();
		List<T> result = db.queryPaged(sql, begin, end, page.isCountTotalSize(), new BeanListProcessor<>(classOfBean), params);
		page.setTotalSize(db.getCounts());
		return result;
	}

	/**
	 * 查询多条数据。
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return List<Map<String, Object>> 不会出现null，如果没有查询到数据，返回的是空 List
	 */
	public static List<Map<String, Object>> queryList(DatabaseWrapper db, String sql, Object... params) {
		return db.query(sql, new MapListProcessor(), params);
	}

	public static List<Map<String, Object>> queryPagedList(DatabaseWrapper db, Page page, String sql, Object... params) {
		if(page.getBeginOfPage()<0) throw new FrameworkRuntimeException("begin不能为负数！");
		if(page.getBeginOfPage()>=page.getEndOfPage()) throw new FrameworkRuntimeException("begin必须小于end！");
		int begin = page.getBeginOfPage();
		int end = page.getEndOfPage();
		List<Map<String, Object>> result = db.queryPaged(sql, begin, end, page.isCountTotalSize(), new MapListProcessor(), params);
		page.setTotalSize(db.getCounts());
		return result;
	}

	/**
	 * 查询并返回一行数据。 慎用！
	 *
	 * 慎用！慎用！慎用！
	 *
	 * 因为，如果查询SQL返回多行数据，本函数返回的是第一行数据。
	 * 对于预期完成得到一行数据，但是又需要检查SQL是否返回了多行数据的情况，请使用 XXXListProcessor！
	 *
	 * 配置了多个DB连接的情况下，需要使用非 default 连接时，使用本函数。
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Object[] 不会出现null，如果没有查询到数据，返回的是空 Object[]
	 */
	public static Object[] queryArray(DatabaseWrapper db, String sql, Object... params) {
		return db.query(sql, new ArrayProcessor(), params);
	}

	/**
	 * 查询多条数据。
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return List<Object[]> 不会出现null，如果没有查询到数据，返回的是空 List
	 */
	public static List<Object[]> queryArrayList(DatabaseWrapper db, String sql, Object... params) {
		return db.query(sql, new ArrayListProcessor(), params);
	}

	/**
	 * 查询多条数据。
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return Result 不会出现null，如果没有查询到数据，返回的是空 Result
	 */
	public static Result queryResult(DatabaseWrapper db, String sql, Object... params) {
		List<Map<String, Object>> result = queryList(db, sql, params);
		return new Result(result);
	}

	public static Result queryPagedResult(DatabaseWrapper db, Page page, String sql, Object... params) {
		List<Map<String, Object>> result = queryPagedList(db, page, sql, params);
		return new Result(result);
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
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params Object 对应sql中的'?'占位符的各个参数值
	 * @return OptionalLong
	 */
	public static OptionalLong queryNumber(DatabaseWrapper db, String sql, Object... params) {
		List<Object[]> result = queryArrayList(db, sql, params);
		if(result.size()!=1) {
			logger.error(String.format("Illegal data rows! There should be only one row, but be %d. sql=[ %s ], params=%s",
					result.size(), sql, Arrays.toString(params)));
			return OptionalLong.empty(); // 只能有一个数据
		}
		try {
			return OptionalLong.of(new Long(result.get(0)[0].toString()));
		} catch (Exception e) {
			logger.error(String.format("Illegal number! sql=[ %s ], params=%s",
					sql, Arrays.toString(params)
			), e);
			return OptionalLong.empty();
		}
	}

	/**
	 * 批量执行增、删、改操作
	 * @param db DatabaseWrapper
	 * @param sql String 带有'?'占位符的sql语句
	 * @param params List<Object[]> 每行是一组对应'?'占位符的数值。
	 *               如果数据很多，比如100万行，构造这么大的List担心OOM，可以分批调用本函数。比如每1万行调用一次。
	 * @return int数组的每一个值代表相应行的数据更新结果
	 */
	public static int[] executeBatch(DatabaseWrapper db, String sql, List<Object[]> params) {
		db.beginTrans();
		return db.execBatch(sql, params);
	}
}
