package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.helper.ResultSetConvertor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class MapListProcessor extends AbstractListProcessor<Map<String, Object>> {
	private final ResultSetConvertor convertor;

	public MapListProcessor() {
		this(ArrayProcessor.BASE_CONVERTOR);
	}

	public MapListProcessor(ResultSetConvertor convertor) {
		this.convertor = convertor;
	}

	@Override
	protected Map<String, Object> handleRow(ResultSet rs, ResultSetMetaData meta, int cols) throws SQLException {
		return this.convertor.toMap(rs, meta, cols);
	}
}
