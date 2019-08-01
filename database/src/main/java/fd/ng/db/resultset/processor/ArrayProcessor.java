package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.helper.ResultSetConvertor;
import fd.ng.db.resultset.ResultSetProcessor;
import fd.ng.db.resultset.helper.CommonsConvertor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 慎用！
 * 因为，如果查询SQL返回多行数据，本处理器返回的是第一行数据。
 * 对于预期完成得到一行数据，但是又需要检查SQL是否返回了多行数据的情况，请使用 XXXListProcessor！
 */
public class ArrayProcessor implements ResultSetProcessor<Object[]> {
	/** 仅仅当前包可见的 */
	static final ResultSetConvertor BASE_CONVERTOR = new CommonsConvertor();
	private final ResultSetConvertor convertor;
	private static final Object[] EMPTY_ARRAY = new Object[0];
	public ArrayProcessor() {
		this(ArrayProcessor.BASE_CONVERTOR);
	}
	public ArrayProcessor(ResultSetConvertor convertor) {
		this.convertor = convertor;
	}

	@Override
	public Object[] handle(ResultSet rs) throws SQLException {
		return rs.next() ? this.convertor.toArray(rs) : EMPTY_ARRAY;
	}
}
