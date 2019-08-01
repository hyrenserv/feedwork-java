package fd.ng.db.jdbc;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 解决使用者在循环内构建DB对象时PreparedStatement重用的问题
 */
class StatementHolder {
	private String sql;
	private PreparedStatement prepStatement;

	public StatementHolder(String sql, PreparedStatement prepStatement) {
		this.sql = sql;
		this.prepStatement = prepStatement;
	}

	/**
	 * @return Returns the prepStatement.
	 */
	public PreparedStatement getPrepStatement()
	{
		return prepStatement;
	}
	/**
	 * @param prepStatement The prepStatement to set.
	 */
	public void setPrepStatement(PreparedStatement prepStatement)
	{
		this.prepStatement = prepStatement;
	}
	/**
	 * @return Returns the sql.
	 */
	public String getSql()
	{
		return sql;
	}
	/**
	 * @param sql The sql to set.
	 */
	public void setSql(String sql)
	{
		this.sql = sql;
	}
}
