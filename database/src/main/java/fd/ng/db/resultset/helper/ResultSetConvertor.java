package fd.ng.db.resultset.helper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把ResultSet转换为各种业务层数据集。
 */
public interface ResultSetConvertor {
	/**
	 * 把ResultSet中一行数据转为Object[]
	 * @param rs ResultSet
	 * @return
	 * @throws SQLException
	 */
	default Object[] toArray(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		return toArray(rs, cols);
	}

	/**
	 * 当外部函数在循环中调用上一个方法时，rs.getMetaData()被循环重复调用，或许影响处理性能。
	 * @param rs ResultSet
	 * @param cols int 列个数
	 * @return
	 * @throws SQLException
	 */
	default Object[] toArray(ResultSet rs, int cols) throws SQLException {
		Object[] result = new Object[cols];

		for (int i = 0; i < cols; i++) {
			result[i] = rs.getObject(i + 1);
		}

		return result;
	}

	/**
	 * 把ResultSet中一行数据转为Map
	 * @param rs ResultSet
	 * @return
	 * @throws SQLException
	 */
	default Map<String, Object> toMap(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		return toMap(rs, rsmd, cols);
	}

	/**
	 * 当外部函数在循环中调用上一个方法时，rs.getMetaData()被循环重复调用，或许影响处理性能。
	 * @param rs ResultSet
	 * @param rsmd ResultSetMetaData 从第一个参数：rs中获取的ResultSetMetaData对象
	 * @param cols int 列个数
	 * @return
	 * @throws SQLException
	 */
	default Map<String, Object> toMap(ResultSet rs, ResultSetMetaData rsmd, int cols) throws SQLException {
		Map<String, Object> result = new HashMap<>(cols);

		for (int i = 0; i < cols; i++) {
			String columnName = rsmd.getColumnName(i+1).toLowerCase();
			result.put(columnName, rs.getObject(i+1));
		}

		return result;
	}

	/**
	 * 把ResultSet中一行数据转为JavaBean
	 * @param rs
	 * @param classOfBean
	 * @param <T>
	 * @return
	 * @throws SQLException
	 */
	default <T> T toBean(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		return null;
	}

	/**
	 * 把ResultSet转为JavaBean List
	 * @param rs
	 * @param classOfBean
	 * @param <T>
	 * @return
	 * @throws SQLException
	 */
	default <T> List<T> toBeanList(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		return null;
	}
}
