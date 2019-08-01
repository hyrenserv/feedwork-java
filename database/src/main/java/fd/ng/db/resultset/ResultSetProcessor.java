package fd.ng.db.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 把ResultSet转换为各种业务层数据集。
 */
public interface ResultSetProcessor<T> {
	/**
	 * 实现ResultSet转换的处理函数
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	T handle(ResultSet rs) throws SQLException;
}
