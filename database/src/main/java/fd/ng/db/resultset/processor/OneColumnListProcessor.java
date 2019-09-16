package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.ResultSetProcessor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OneColumnListProcessor<T> implements ResultSetProcessor<List<T>> {

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		if(meta.getColumnCount()<1) return Collections.emptyList();
		List<T> rows = new ArrayList<T>();
		while (rs.next()) {
			rows.add((T)rs.getObject(1));
		}
		return rows;
	}
}
