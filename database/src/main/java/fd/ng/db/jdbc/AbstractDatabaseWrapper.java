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
			pstmt.setNull(i, Types.NULL);
		} else {
			pstmt.setObject(i, param);
		}
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
}
