package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.helper.ResultSetConvertor;
import fd.ng.db.resultset.ResultSetProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanProcessor<T> implements ResultSetProcessor<T> {

	private final Class<? extends T> classTypeOfBean;
	private final ResultSetConvertor convertor;

	public BeanProcessor(Class<? extends T> classTypeOfBean) {
		this(classTypeOfBean, ArrayProcessor.BASE_CONVERTOR);
	}
	public BeanProcessor(Class<? extends T> classTypeOfBean, ResultSetConvertor convertor) {
		this.classTypeOfBean = classTypeOfBean;
		this.convertor = convertor;
	}

	@Override
	public T handle(ResultSet rs) throws SQLException {
		return this.convertor.toBean(rs, this.classTypeOfBean);
	}
}
