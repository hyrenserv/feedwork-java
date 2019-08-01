package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.helper.ResultSetConvertor;
import fd.ng.db.resultset.ResultSetProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BeanListProcessor<T> implements ResultSetProcessor<List<T>> {
	private final Class<? extends T> classTypeOfBean;
	private final ResultSetConvertor convertor;

	public BeanListProcessor(Class<? extends T> classTypeOfBean) {
		this(classTypeOfBean, ArrayProcessor.BASE_CONVERTOR);
	}

	public BeanListProcessor(Class<? extends T> classTypeOfBean, ResultSetConvertor convertor) {
		this.classTypeOfBean = classTypeOfBean;
		this.convertor = convertor;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		return this.convertor.toBeanList(rs, classTypeOfBean);
	}
}
