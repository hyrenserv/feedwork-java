package fd.ng.db.jdbc;

import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.UuidUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.DBException;
import fd.ng.db.conf.ConnWay;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.nature.*;
import fd.ng.db.resultset.ResultSetProcessor;
import fd.ng.db.util.SqlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * 建议：项目启动后首先加载配置信息 ：ClassUtil.loadClass(DbinfosConf.class.getName());
 * 以便完成各种初始化工作，比如自动加载 POOL 方式的 DataSouce。
 */
public class DatabaseWrapper extends AbstractDatabaseWrapper {
	private static final Logger logger = LogManager.getLogger(DatabaseWrapper.class.getName());
	public static final int NULL_FETCH_SIZE = -238949899; // 随便一个不会被用到的数字

	private Connection conn; // db连接
	private final boolean lazyConnect;
	private final DbinfosConf.Dbinfo dbinfo; // db配置
	private final String id; // 代表当前db对象，（比如，WEB类应用如果有自己的id可传入）
	private final String traceId; // 打印到日志里面的id标识： id-name
	private final String desc; // 对当前db对象的详细描述
	private boolean autoCommit; // 是否自动提交事务，同时也代表是否启动了事务（为false表示启动了事务）
	/**
	 * 设置FetchSize作用：
	 * 1）避免查询大表时导致OOM。比如一张5千万的表，用 select * from 查询返回 ResultSet ，会在长时间等待后直接OOM
	 * 2）对检索大数据表时性能提升明显。
	 * 一般设置为：100或200或500即可。
	 *
	 * 1） mysql
	 *      1）url加上 useCursorFetch=true&defaultFetchSize=500
	 *      2）代码中设置： setFetchSize(Integer.MIN_VALUE);
	 *      3）要使用 5.1 以上的 jdbc jar
	 * 2） postgresql
	 *      设置 autoCommit = false ，代码中 setFetchSize(500)
	 *      要使用 42.2 以上的 jdbc jar
	 * 3） oracle
	 *      设置为500一般就可以。
	 *      另外一种方式：
	 *      4 * 1024 * 1024 / sum(data_length for all columns of your table) 实测会快。1亿表，500为320秒，用这个公式算的为240秒
	 */
	private int fetchSize=NULL_FETCH_SIZE; // 初始设置-99为了后面判断是否给他设置了值
	private boolean showsql; // 默认使用dbinfo中的配置，所以，这里默认为null。用于在程序中关闭SQL日志输出

	private int countsInPagedQuery; // 分页查询时，SQL的总记录数

	public DatabaseWrapper() { this(new Builder()); }
	private DatabaseWrapper(Builder builder) {
		if(builder.dbinfo==null) this.dbinfo = DbinfosConf.getDatabase(builder.dbname);
		else this.dbinfo = builder.dbinfo; // 可以没有conf文件，使用编程方式构造这个对象

		if(builder.noid) this.id = StringUtil.EMPTY;
		else { // 需要id
			if(builder.id==null) this.id = Long.toString(UuidUtil.elapsedNanoTime());
			else this.id = builder.id;
		}

		this.desc = builder.desc;

		this.lazyConnect = builder.lazyConnect;

		this.conn = builder.conn;
		// 如果外部传入了conn，那么直接设置成“已连接”的状态
		if(this.conn==null) this.connected = false;
		else this.connected = true;

		if(builder.autoCommit==null) this.autoCommit = this.dbinfo.autoCommit();
		else this.autoCommit = builder.autoCommit;
		if(builder.showsql==null) this.showsql = this.dbinfo.show_sql();
		else this.showsql = builder.showsql;
		if(builder.fetchSize == NULL_FETCH_SIZE) { // 没有在代码中显示设置 fetchSize
			if(this.dbinfo.hasFetch_size())
				this.fetchSize = this.dbinfo.getFetch_size();
		}
		else this.fetchSize = builder.fetchSize;

		// 内部值
		this.beginTrans = false;
		this.beginCount = 0;
		this.commit = false;
		this.commitCount = 0;
		this.rollback = false;
		this.rollbackCount = 0;
		this.updatingInTrans = false;

		if(dbinfo==null)
			throw new DBException(this.id, "Can not get database conf info by name : " + builder.dbname);

		this.traceId = this.id+"-DB-"+this.dbinfo.getName();

		if(logger.isTraceEnabled())
			logger.trace("{} used database conf : {}", this.traceId, this.dbinfo.toString());

		if(!this.lazyConnect) {
			setupConnection();
			if(!this.autoCommit) // 创建连接时，设置成了非自动事务，也就意味着“启动了事务”
				this.beginTrans();
		}
	}

	/**
	 * 无特殊原因不要使用本方法
	 * @return 内部创建并使用的 Connection
	 */
	public Connection getConnection() {
		return this.conn;
	}
	public String getName() { return this.dbinfo.getName(); }
	public String getID() { return this.id; }
	public Dbtype getDbtype() { return this.dbinfo.getDbtype(); }
	public DbinfosConf.Dbinfo ofInfo() { return this.dbinfo; }

	/**
	 * 如果是lazyConnect方式创建的对象，当需要真正建立连接时，调用本方法
	 */
	public DatabaseWrapper makeConnection() {
		if(this.lazyConnect) {
			setupConnection();
			if(!this.autoCommit) // 创建连接时，设置成了非自动事务，也就意味着“启动了事务”
				this.beginTrans();
		}
		return this;
	}

	private void setupConnection() {
		if(this.connected) return;
		try {
			if(this.conn!=null) {
				logger.trace("{} reUse  autoCommit={}", this.traceId, this.conn.getAutoCommit());
				return;
			}
			long start = 0;
			if (ConnWay.JDBC == this.dbinfo.getWay()) {
				if(dbinfo.isShow_conn_time()) start = System.currentTimeMillis();
				Class.forName(this.dbinfo.getDriver());
				this.conn = DriverManager.getConnection(
						this.dbinfo.getUrl(), this.dbinfo.getUsername(), this.dbinfo.getPassword());
			} else if (ConnWay.POOL == this.dbinfo.getWay()) {
				if(dbinfo.isShow_conn_time()) start = System.currentTimeMillis();
				this.conn = DbinfosConf.DATA_SOURCE.get(this.getName()).getConnection();
			} else if (ConnWay.JNDI == this.dbinfo.getWay()) {
				if(dbinfo.isShow_conn_time()) start = System.currentTimeMillis();
				javax.naming.InitialContext initialcontext = new javax.naming.InitialContext();
				javax.sql.DataSource ds = (javax.sql.DataSource)initialcontext.lookup(this.dbinfo.getUrl());
				conn = ds.getConnection();
			} else {
				throw new DBException(this.traceId, " Unsupported connection way : " + dbinfo.getWay());
			}
			if(logger.isInfoEnabled())
				logger.info("{} NewDB by {} {} AutoCommit {} -> {}",
						this.traceId, this.dbinfo.getWay(), this.desc, this.conn.getAutoCommit(), this.autoCommit);
			if(dbinfo.isShow_conn_time()) {
				logger.info("{} make  connection time : {}ms", this.traceId, (System.currentTimeMillis()-start));
			}
			this.connected = true;
			if(this.autoCommit)
				conn.setAutoCommit(true);
			else
				beginTrans();
		} catch (Exception e) {
			throw new DBException(this.id, this.dbinfo==null?"dbinfo is null!":this.dbinfo.toString(), e);
		}
	}

	/**
	 * 增删改数据
	 * @param sql String 使用 '?' 占位的SQL语句
	 * @param params Object[] 每一个占位参数
	 * @return int 该update sql产生的DB变化数量
	 */
	public int execute(final String sql, Object... params) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		try {
			if(isBeginTrans()) this.updatingInTrans = true;
			PreparedStatement curPstmt = getPreparedStatement(sql, false);
			fillStatementParameters(0, curPstmt, params);
			long start=0;
			if(this.dbinfo.isShow_sql_time()) start = System.currentTimeMillis();
			int nums = curPstmt.executeUpdate();
			if(this.showsql&&logger.isInfoEnabled())
				logger.info("{} ExecU sql : [ {} ]", this.traceId, SqlUtil.getGoodshowSql(-1, -1, sql, params));
			if(this.dbinfo.isShow_sql_time()) {
				long end = System.currentTimeMillis();
				long et = end - start;
				if( et>this.dbinfo.getLongtime_sql() )
					logger.warn("{} [LONGTIME SQL(EXECUTE)] elapse time {}ms, sql=[ {} ]", this.traceId, et, sql);
			}
			return nums;
		} catch (SQLException e) {
			throw this.reSQLException(e, sql, params);
		}
	}

	/**
	 *
	 * @param sql String 使用 '?' 占位的SQL语句
	 * @param paramsList List<Object[]> 所有需要批量提交的数据
	 * @return int[] 每一个值代表 paramsList 中相应下标的数据提交的DB更新结果数量
	 */
	public int[] execBatch(final String sql, List<Object[]> paramsList) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		try {
			if(isBeginTrans()) this.updatingInTrans = true;
			PreparedStatement curPstmt = getPreparedStatement(sql, false);
			int size = paramsList.size();
			// TODO 改成每个5000行提交一次？
			for (int i = 0; i < size; i++) {
				fillStatementParameters(0, curPstmt, paramsList.get(i));
				curPstmt.addBatch();
			}
			long start=0;
			if(this.dbinfo.isShow_sql_time()) start = System.currentTimeMillis();
			int[] nums = curPstmt.executeBatch();
			if(this.showsql&&logger.isInfoEnabled())
				logger.info("{} ExecB batch sql : [ {} ], params : {}, {} ... ... params.size={}", this.traceId,
						sql, Arrays.toString(paramsList.get(0)), Arrays.toString(paramsList.get(1)), size);
			if(this.dbinfo.isShow_sql_time()) {
				long end = System.currentTimeMillis();
				long et = end - start;
				if( et>this.dbinfo.getLongtime_sql() )
					logger.warn("{} [LONGTIME SQL(EXECUTE BATCH)] elapse time {}ms, sql=[ {} ]", this.traceId, et, sql);
			}
			return nums;
		} catch (SQLException e) {
			if(paramsList==null||paramsList.size()<1)
				throw this.reSQLException(e,"(BATCH params size=0) " + sql);
			else
				throw this.reSQLException(e,"(BATCH params size="+paramsList.size()+") " + sql, paramsList.get(0));
		}
	}

	/**
	 * 查询数据
	 * @param sql String 使用 '?' 占位的SQL语句
	 * @param rsProcessor ResultSetProcessor
	 * @param params Object[] 每一个占位参数
	 * @param <T>
	 * @return
	 */
	public <T> T query(final String sql, ResultSetProcessor<T> rsProcessor, Object... params) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		ResultSet rs = queryGetResultSet(sql, params);
		try {
			T obj = rsProcessor.handle(rs);
			return obj;
		} catch (SQLException e) {
			throw this.reSQLException(e, sql, params);
		} finally {
			try{ if(rs!=null) rs.close(); } catch (SQLException e) { logger.warn(e); }
		}
	}

	/**
	 * 查询得到 ResultSet ，一般不需要调用，请使用 query 函数。
	 * @param sql String 使用 '?' 占位的SQL语句
	 * @param params Object[] 每一个占位参数
	 * @return ResultSet
	 */
	public ResultSet queryGetResultSet(final String sql, Object... params) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		ResultSet rs = null;
		try {
			rs = doQueryGetResultSet(sql, 0, 0, false, params);
			return rs;
		} catch (SQLException e) {
			try{ if(rs!=null) rs.close(); rs=null; }catch(Exception ex){}
			throw this.reSQLException(e, sql, params);
		}
	}

	public <T> T queryPaged(final String sql, int begin, int end, ResultSetProcessor<T> rsProcessor, Object... params) {
		return queryPaged(sql, begin, end, true, rsProcessor, params);
	}
	public <T> T queryPaged(final String sql, int begin, int end, boolean isCountTotal, ResultSetProcessor<T> rsProcessor, Object... params) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		ResultSet rs = queryPagedGetResultSet(sql, begin, end, isCountTotal, params);
		try {
			T obj = rsProcessor.handle(rs);
			return obj;
		} catch (SQLException e) {
			throw this.reSQLException(e, sql, params);
		} finally {
			try{ if(rs!=null) rs.close(); } catch (SQLException e) { logger.warn(e); }
		}
	}
	/**
	 * 分页查询时，通过该函数获得总记录数。
	 * @return int
	 */
	public int getCounts(){
		return this.countsInPagedQuery;
	}

//	public ResultSet queryPagedGetResultSet(final String sql, int begin, int end, Object... params) {
//		return queryPagedGetResultSet(sql, begin, end, true, params);
//	}

	/**
	 *
	 * @param sql
	 * @param begin         开始位置，从1开始
	 * @param end           结束位置，包含这个值
	 * @param isCountTotal
	 * @param params
	 * @return
	 */
	public ResultSet queryPagedGetResultSet(String sql, int begin, int end, boolean isCountTotal, Object... params) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		if(isCountTotal) {
			String count_sql = getDbtype().ofCountSql(sql);
			try(ResultSet rs = doQueryGetResultSet(count_sql, 0, 0, false, params)) {
				if(rs.next()) {
					this.countsInPagedQuery = rs.getInt(1);
				} else {
					throw new DBException(this.traceId, "sql exception. sql : " + count_sql);
				}
			} catch (SQLException e) {
				throw this.reSQLException(e, count_sql, params);
			}
		}
		PagedSqlInfo pagedSqlInfo = getDbtype().ofPagedSql(sql, begin, end);
		sql = pagedSqlInfo.getSql();
		ResultSet rs = null;
		try {
			rs = doQueryGetResultSet(sql
					, pagedSqlInfo.getPageNo1(), pagedSqlInfo.getPageNo2(), true, params);
			return rs;
		} catch (SQLException e) {
			try{ if(rs!=null) rs.close(); }catch(Exception ex){}
			throw this.reSQLException(e, sql, params);
		}
	}
	private ResultSet doQueryGetResultSet(final String sql, int begin, int end, boolean isPaged, Object... params) throws SQLException {
		PreparedStatement curPstmt = getPreparedStatement(sql, true);
		if(logger.isTraceEnabled()) {
			logger.trace("{} autoCommit={}, fetchSize={}, fetchDirection={}", this.traceId,
					conn.getAutoCommit(), curPstmt.getFetchSize(), curPstmt.getFetchDirection());
		}
		int pstmtIndex = fillStatementParameters(0, curPstmt, params);
		if(isPaged) {// 是分页
			if(pstmtIndex<0) pstmtIndex = 0;
			if(end== PagedSqlInfo.PageNoValue_NotExist)
				fillStatementParameters(pstmtIndex, curPstmt, begin);
			else
				fillStatementParameters(pstmtIndex, curPstmt, begin, end);
		}
		long startTime=0L;
		if(this.dbinfo.isShow_sql_time()) startTime = System.currentTimeMillis();
		ResultSet rs = curPstmt.executeQuery();
		if(this.showsql&&logger.isInfoEnabled()) {
			if(isPaged)
				logger.info("{} Query sql : [ {} ]", this.traceId, SqlUtil.getGoodshowSql(begin, end, sql, params));
			else
				logger.info("{} Query sql : [ {} ]", this.traceId, SqlUtil.getGoodshowSql(-1, -1, sql, params));
		}
		if(this.dbinfo.isShow_sql_time()) {
			long endTime = System.currentTimeMillis();
			long et = endTime - startTime;
			if( et>this.dbinfo.getLongtime_sql() )
				logger.warn("{} [LONGTIME SQL(SELECT)] elapse time {}ms, sql=[ {} ]", this.traceId, et, sql);
		}
		return rs;
	}

	/**
	 * 使用自己定义的ResultSetProcessor转换查询结果，代码样例：<br>
	 *     Object[] rs = assembleResultSet(rs,
	 *         new ResultSetProcessor<Object[]>() {
	 *             public Object[] handle(ResultSet rs) throws SQLException {
	 *                 ResultSetMetaData meta = rs.getMetaData();
	 *                 int cols = meta.getColumnCount();
	 *                 Object[] result = new Object[cols];
	 *                 for (int i = 0; i < cols; i++) {
	 *                     result[i] = rs.getObject(i + 1);
	 *                 }
	 *                 return result;
	 *             }
	 *         }
	 *     );
	 * 一般不需要使用本函数，在 {@link SqlOperator} 中已经完成了各种易用函数的封装。
	 * @param rs
	 * @param rsProcessor
	 * @param <T>
	 * @return
	 */
	public <T> T assembleResultSet(ResultSet rs, ResultSetProcessor<T> rsProcessor, boolean isCloseResultSet) {
		Validator.notNull(rs, "Null ResultSet");
		Validator.notNull(rsProcessor, "Null ResultSetProcessor");
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		try {
			return rsProcessor.handle(rs);
		} catch (SQLException e) {
			throw new DBException(this.traceId, e);
		} finally {
			if(isCloseResultSet)
				try{ if(rs!=null) rs.close(); } catch (SQLException e) { logger.warn(e); }
		}
	}

	/**
	 * DDL指：create table、drop table、create index等。
	 * @param sql String
	 * @return
	 */
	public int ExecDDL(String sql) {
		if(!this.connected) throw new DBException(this.traceId, "no connection!");
		try( Statement stmt = this.conn.createStatement() ) {
			int num = stmt.executeUpdate(sql);
			logger.info("{} doDDL sql : {}", this.traceId, sql);
			return num;
		} catch (Exception e) {
			throw new DBException(this.traceId, "ddl failed! sql : " + sql, e);
		}
	}

	public void beginTrans() {
		if(!this.connected) return;
		if(isBeginTrans()){
			if(logger.isTraceEnabled())
				logger.trace("{} Trans already begin.", this.traceId);
			return;
		}
		try{
			this.autoCommit = false;
			this.conn.setAutoCommit(false);
			this.beginTrans = true;
			this.beginCount++;
			if(this.showsql&&logger.isInfoEnabled())
				logger.info("{} Trans begin successful", this.traceId);
		} catch(Exception e){
			throw new DBException(this.traceId, "beginTrans fail!", e);
		}
	}
	public void commit() {
		if(!this.connected) return;
		try{
			if(isBeginTrans()){
				this.conn.commit();
				this.commit = true;
				this.commitCount++;
				this.updatingInTrans = false;
				if(this.showsql&&logger.isInfoEnabled())
					logger.info("{} Trans commit", this.traceId);
			}
//			else {
//				logger.warn("{} Trans 没有开启事务，无法执行事务提交操作", this.traceId);
//			}
		}catch(Exception e){
			throw new DBException(this.traceId, "commit fail!", e);
		}
	}
	public void rollback() {
		if(!this.connected) return;
		try{
			if(isBeginTrans()){
				this.conn.rollback();
				this.rollback = true;
				this.rollbackCount++;
				this.updatingInTrans = false;
				if(this.showsql&&logger.isInfoEnabled())
					logger.info("{} Trans rollback", this.traceId);
			}
//			else {
//				logger.warn("{} Trans 没有开启事务，无法执行事务回滚操作", this.traceId);
//			}
		}catch(Exception e){
			throw new DBException(this.traceId, "rollback fail!", e);
		}
	}

	@Override
	public void close()	{
		if(!this.connected) return;
		try{ // 清理 PrepStatement
			for (StatementHolder stmt : this.pstmtBox.values()) {
				if(stmt.getPrepStatement()!=null) stmt.getPrepStatement().close(); stmt.setPrepStatement(null);
			}
			pstmtBox.clear();
		}catch(Exception e){
			logger.error(this.traceId + " clear PrepStatement failed", e);
		}
		try { // 清理未完成的事务
			if(isUpdatingInTrans()) {
				// 开始了事务，但是直到关闭，既没有提交事务，也没有回滚事务。
//				if(isBeginTrans()&&!isCommit()&&!isRollback())
				{
					logger.error("{} Trans has unhandled, auto rollback before close", this.traceId);
					this.conn.rollback();
				}
			}
		} catch (Exception e) {
			logger.error(this.traceId + " rollback failed in db.close()", e);
		}
		try{ // 关闭连接
			if(this.conn!=null&&!this.conn.isClosed()){
				this.conn.close();
				this.connected = false;
				logger.info("{} Close ", this.traceId);
			}
		} catch(Exception e) {
			throw new DBException(this.traceId, "close connection failed", e);
		}
	}

	/**
	 * 检查表是否存在
	 *
	 * @param tableName 要获取的表的名称
	 * @return true 加入存在
	 */
	public boolean isExistTable(String tableName) {
		ResultSet rsThis = null;
		try {
			DatabaseMetaData dbMeta = this.conn.getMetaData();
			// 对于pgsql， 第2个参数导致无法获取到表
			if(dbinfo.getDbtype()==Dbtype.MYSQL) // 目前只测试 mysql 可用 schemaPattern， pgsql不能用 schemaPattern
				rsThis = dbMeta.getTables(null, this.dbinfo.getUsername(), tableName, null);
			else
				rsThis = dbMeta.getTables(null, null, tableName, null);
			if(rsThis.next()) return true;
			else {
//				rsThis.close();
//				rsThis = queryGetResultSet("select count(1) from " +tableName + " where 1=2");
//				if(rsThis.next()) return true;
//				else
					return false;
			}
		} catch (SQLException e) {
			throw new DBException(this.traceId, "ExistCheck table '"+tableName+"' failed", e);
		} finally {
			try{ if(rsThis!=null) rsThis.close(); }catch(Exception e){}
		}
	}

	private PreparedStatement getPreparedStatement(final String curSQL, boolean isSelectStatement) throws SQLException {
		PreparedStatement curPstmt;
		StatementHolder pstmtHolder = this.pstmtBox.get(curSQL);
		if(pstmtHolder==null) {
			if(isSelectStatement) {
				curPstmt = this.conn.prepareStatement(curSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				if(fetchSize!=NULL_FETCH_SIZE)
					curPstmt.setFetchSize(fetchSize);
//				if(fetchDirection)
//					curPstmt.setFetchDirection(fetchDirection);
			}
			else
				curPstmt = this.conn.prepareStatement(curSQL);
			pstmtHolder = new StatementHolder(curSQL, curPstmt);
			this.pstmtBox.put(curSQL, pstmtHolder);
		} else {
			curPstmt = pstmtHolder.getPrepStatement();
		}
		return curPstmt;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.traceId);
		if(StringUtil.isNotEmpty(this.desc)) sb.append(" (").append(desc).append(')');
		sb.append(" : ");

		if(this.conn==null) { // lazy方式创建DB时
			return sb.append("db connection is null.").toString();
		}
		sb.append("conn=").append(this.conn);
		try {
			sb.append(", conn's autoCommit=").append(this.conn.getAutoCommit());
		} catch (SQLException e) {
			sb.append(", call conn.getAutoCommit() fail! Exception : ").append(e.toString());
		}
		if (isBeginTrans()) { // 启动了事务
			sb.append(", used transaction : ").append(getTransInfo());
		} else {
			sb.append(", No Transaction.");
		}
		return sb.toString();
	}

	public static final class Builder {
		private String dbname;
		private String desc;
		private boolean lazyConnect;
		private DbinfosConf.Dbinfo dbinfo;
		private Connection conn;
		private String id;
		private boolean noid;
		private Boolean autoCommit;
		private Boolean showsql;
		private int fetchSize = NULL_FETCH_SIZE;

		/**
		 * 这里设置默认值
		 */
		public Builder() {
			dbname = DbinfosConf.DEFAULT_DBNAME;
			desc = StringUtil.EMPTY;
			dbinfo = null;
			id = null;
			autoCommit = null;
			conn = null;
			lazyConnect = false;
			noid = false; // 默认需要id
			showsql = null; // 默认使用dbinfo中的配置，所以，这里默认为null。用于在程序中关闭SQL日志输出
		}

		public String getDbname() { return dbname; }

		public Builder dbname(String val) {
			dbname = val;
			return this;
		}

		public Builder desc(String val) {
			desc = val;
			return this;
		}

		public Builder dbconf(DbinfosConf.Dbinfo val) {
			dbinfo = val;
			return this;
		}

		public Builder injectConnection(Connection val) {
			conn = val;
			return this;
		}

		public Builder lazyConnect(boolean val) {
			lazyConnect = val;
			return this;
		}

		public Builder id(String val) {
			id = val;
			return this;
		}

		public Builder noid(boolean val) {
			noid = val;
			return this;
		}

		public Builder autoCommit(boolean val) {
			autoCommit = val;
			return this;
		}

		public Builder showsql(boolean val) {
			showsql = val;
			return this;
		}

		public Builder fetchSize(int val) {
			fetchSize = val;
			return this;
		}

		/**
		 * 必须最后被调用！！！
		 * @return DatabaseWrapper
		 */
		public DatabaseWrapper create() {
			if(dbname!=null&&dbinfo!=null) {
				dbname = dbinfo.getName();
				logger.warn("dbname and dbinfo is alternately, will be used dbinfo, ignore dbname(={})", dbname);
			}
			return new DatabaseWrapper(this);
		}
	}
}
