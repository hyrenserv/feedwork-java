package fd.ng.db.jdbc;

import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.BeanUtil;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractDatabaseWrapper implements AutoCloseable {
	protected final Map<String, StatementHolder> pstmtBox = new HashMap<>();

	protected boolean connected; // 是否建立了DB连接（lazy方式不会设置为true
	protected boolean beginTrans; // 是否启动事务
	protected boolean commit; // 是否执行过一次 commit 操作
	protected boolean rollback; // 是否执行过一次 rollback 操作

	// 记录在启动事务的情况下，是否开始执行增删改操作
	// 处理逻辑为：初始为 false；调用了execute函数时，如果启动了事务则设置为 true，执行了commit/rollbakc后被设置为false（意味着执行结束了）
	protected boolean updatingInTrans;

	protected int beginCount; // 调用 beginTrans 的次数
	protected int commitCount; // 调用 commit 的次数
	protected int rollbackCount; // 调用 rollback 的次数

	public boolean isUpdatingInTrans() {
		return updatingInTrans;
	}
	public boolean isConnected() {
		return connected;
	}
	public boolean isBeginTrans() {
		return beginTrans;
	}
	public boolean isCommit() {
		return commit;
	}
	public boolean isRollback() {
		return rollback;
	}

	public int getBeginCount() {
		return beginCount;
	}
	public int getCommitCount() {
		return commitCount;
	}
	public int getRollbackCount() {
		return rollbackCount;
	}

	public String getTransInfo() {
		return "Transaction{connected=" + connected +
				", begin=" + beginTrans +
				", commit=" + commit +
				", rollback=" + rollback +
				", beginCount=" + beginCount +
				", commitCount=" + commitCount +
				", rollbackCount=" + rollbackCount +
				'}';
	}

	// 返回 PreparedStatement 设置的最大下标值
	protected int fillStatementParameters(int begin, final PreparedStatement pstmt, Object... params) throws SQLException {
		if (params == null ) {
			return 0;
		}
		if ( params.length==1 ) {
			if( params[0] instanceof List ) {
				List paramList = ((List) params[0]);
				int end = paramList.size();
				int pstmtIndex = 0;
				for (int i = 0; i < end; i++) {
					Object param = paramList.get(i);
					pstmtIndex = begin + i + 1;
					fillStatementParameter(pstmt, pstmtIndex, param);
				}
				return pstmtIndex;
			}
		}

		int end = params.length;
		int pstmtIndex = 0;
		for (int i = 0; i < end; i++) {
			Object param = params[i];
			pstmtIndex = begin + i + 1;
			fillStatementParameter(pstmt, pstmtIndex, param);
		}
		return pstmtIndex;
	}
	private void fillStatementParameter(final PreparedStatement pstmt, int i, Object param) throws SQLException {
		if (param == null) {
			// 不论怎样，VARCHAR 都能适应各种JDBC驱动，且不管该列的真实类型是什么。
			// Types.NULL 和 Types.OTHER 对Oracle驱动好像有问题？
			pstmt.setNull(i, Types.VARCHAR);
		} else {
			pstmt.setObject(i, param);
		}
	}

	// begin,end针对的是分页查询。如果不是分页，传入-1，用于内部逻辑判断是否有这两个参数
	protected String getGoodshowSql(int begin, int end, String sql, Object... params)
	{
		int i=0,loc=0;
		int paramSize = params.length;
		if ( paramSize==1 ) {
			if( params[0] instanceof List ) {
				List paramList = ((List) params[0]);
				paramSize = paramList.size();
				params = paramList.toArray(new Object[paramSize]);
			}
		}
		while(loc>-1){
			loc = sql.indexOf('?');
			if(loc<0) return sql;
			if(i>=paramSize){//即将要从params中取的数据已经超出了params中数据的个数。因为params是从0开始取数，所以这里是>=
				if(begin==-1) // 无分页
					throw new RawlayerRuntimeException("SQL=["+sql+"] error. more '?' than param.");
				else {
					sql = sql.replaceFirst("\\?", String.valueOf(begin));
					sql = sql.replaceFirst("\\?", String.valueOf(end));
					if(sql.contains("?"))
						throw new RawlayerRuntimeException("Paged SQL=["+sql+"] error. more '?' than param.");
					else
						return sql;
				}
			}
			Object param=params[i++];
			if(param==null) {
				sql=sql.replaceFirst("\\?","null");
			} else if (BeanUtil.isNumberClass(param)){
				sql=sql.replaceFirst("\\?",param.toString());
			} else if(param instanceof String){
				String tmp_param = (String) param;
				tmp_param = tmp_param.replaceAll("\\?", "？");
				sql=sql.replaceFirst("\\?","'"+tmp_param+"'");
			} else{
				sql=sql.replaceFirst("\\?","'"+param.toString()+"'");
			}
		}
		return sql;
	}

	protected RawlayerRuntimeException reSQLException(SQLException cause, String sql, Object... params) {
		StringBuilder additionalMsg = new StringBuilder();
		additionalMsg.append("\n SQL: ");
		additionalMsg.append(sql);
		additionalMsg.append("\n Parameters: ");
		if (params == null) {
			additionalMsg.append("[]\n");
		} else {
			additionalMsg.append(Arrays.deepToString(params)).append("\n");
		}

		String SQLState = cause.getSQLState();
		if(SQLState!=null) additionalMsg.append(" SQLState=").append(SQLState);
		additionalMsg.append(" SQLErrorCode=").append(cause.getErrorCode()).append("\n");
		return new RawlayerRuntimeException("SQL processing exception", additionalMsg.toString(), cause);
	}

//	/**
//	 * 对各种数据库进行分类：
//	 * MSSQL：
//	 * SELECT TOP 10 * FROM TestTable WHERE (ID NOT IN (SELECT TOP 20 id FROM TestTable ORDER BY id)) ORDER BY ID
//	 * DB2：
//	 * SELECT * FROM (Select 字段1,字段2,字段3,rownumber() over(ORDER BY 排序用的列名 ASC) AS rn from 表名) AS a1 WHERE a1.rn BETWEEN 10 AND 20
//	 * select * from (select rownumber() over(order by id asc ) as rowid from table where rowid <=endIndex ) where rowid > startIndex
//	 * 对于 DB2 AS400 没有rownumber，所以需要再想办法。
//	 *
//	 * @param sql
//	 * @param hasOffset
//	 * @return
//	 */
//	protected String getLimitString(String sql, Dbtype dbtype, boolean hasOffset)
//	{
//		if(dbtype==Dbtype.ORACLE){
//			StringBuilder pagingSelect = new StringBuilder( sql.length()+100 );
//			if (hasOffset) {
//				pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
//			}
//			else {
//				pagingSelect.append("select * from ( ");
//			}
//			pagingSelect.append(sql);
//			if (hasOffset) {
//				pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
//			}
//			else {
//				pagingSelect.append(" ) where rownum <= ?");
//			}
//			return pagingSelect.toString();
//		}
//		else if(dbtype==Dbtype.MYSQL){
//			StringBuilder pagingSelect = new StringBuilder( sql.length()+20 );
//			pagingSelect.append(sql);
//			if (hasOffset) {
//				pagingSelect.append(" limit ?,?");
//			}
//			else {
//				pagingSelect.append(" limit ?");
//			}
//			return pagingSelect.toString();
//		}
//		else if(dbtype==Dbtype.DB2V1){
//			StringBuilder pagingSelect = new StringBuilder( sql.length()+200 );
//			if (!hasOffset) {
//				pagingSelect.append(sql).append(" fetch first ? rows only");
//			}
//			else{
//				pagingSelect.append( "select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ")
//						.append( sql )
//						.append( " fetch first db2_key_limit" )//limit
//						.append( " rows only ) as inner2_ ) as inner1_ where rownumber_ > db2_key_offset" )//offset
//						.append( " order by rownumber_" );
//			}
//			return pagingSelect.toString();
//		}
//		else if(dbtype==Dbtype.DB2V2){
//			int startOfSelect = sql.toLowerCase().indexOf("select");
//
//			StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
//			pagingSelect.append(sql.substring(0, startOfSelect));
//			pagingSelect.append("select * from ( select ");
//			pagingSelect.append(getRowNumberH3(sql));
//
//			//if( hasDistinctH3(sql) )有没有Distinct，都应该用下面的语句把原来的SQL套上一层，否则报错。
//			{
//				pagingSelect.append(" row_.* from ( ")
//						.append(sql.substring(startOfSelect))
//						.append(" ) as row_");
//			}
////			else
////			{
////				pagingSelect.append(sql.substring(startOfSelect + 6)); // add the main query
////			}
//
//			pagingSelect.append(" ) as temp_ where rownumber_ ");
//
//			//add the restriction to the outer select
//			if( hasOffset )
//			{
//				pagingSelect.append("between ?+1 and ?");
//			}
//			else
//			{
//				pagingSelect.append("<= ?");
//			}
//
//			return pagingSelect.toString();
//		}
//		else{
//			throw new RawlayerRuntimeException("Unsupport db type : ["+dbtype+"] in paged sql conversion!");
//		}
//	}

//	private String getRowNumberH3(String sql)
//	{
//		StringBuilder rownumber = new StringBuilder(50).append("rownumber() over(");
//
//		/*下面语句目的是再over中把order by加进去，可是，再DB29.7上试验的时候报错，而不加确能通过*/
////		int orderByIndex = sql.toLowerCase().indexOf("order by");
////		if( orderByIndex > 0 && !hasDistinctH3(sql) )
////		{
////			rownumber.append(sql.substring(orderByIndex));
////		}
//
//		rownumber.append(") as rownumber_,");
//
//		return rownumber.toString();
//	}
}
