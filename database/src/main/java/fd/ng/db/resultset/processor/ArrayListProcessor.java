package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.helper.ResultSetConvertor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ArrayListProcessor extends AbstractListProcessor<Object[]> {
	private final ResultSetConvertor convertor;
	public ArrayListProcessor() {
		this(ArrayProcessor.BASE_CONVERTOR);
	}
	public ArrayListProcessor(ResultSetConvertor convertor) {
		this.convertor = convertor;
	}
	@Override
	protected Object[] handleRow(ResultSet rs, ResultSetMetaData meta, int cols) throws SQLException {
		return this.convertor.toArray(rs, cols);
	}
}
