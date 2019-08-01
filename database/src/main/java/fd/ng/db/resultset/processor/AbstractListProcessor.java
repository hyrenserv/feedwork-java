package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.ResultSetProcessor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListProcessor<T> implements ResultSetProcessor<List<T>> {
	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		List<T> rows = new ArrayList<T>();
		while (rs.next()) {
			rows.add(this.handleRow(rs, meta, cols));
		}
		return rows;
	}
	protected abstract T handleRow(ResultSet rs, ResultSetMetaData meta, int cols) throws SQLException;
}
