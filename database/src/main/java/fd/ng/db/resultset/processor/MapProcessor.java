package fd.ng.db.resultset.processor;

import fd.ng.db.resultset.TooManyRecordsException;
import fd.ng.db.resultset.helper.ResultSetConvertor;
import fd.ng.db.resultset.ResultSetProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 慎用！
 * 因为，如果查询SQL返回多行数据，本处理器返回的是第一行数据。
 * 对于预期完成得到一行数据，但是又需要检查SQL是否返回了多行数据的情况，请使用 XXXListProcessor！
 */
public class MapProcessor implements ResultSetProcessor<Map<String, Object>> {
	private final ResultSetConvertor convertor;
	private static final Map<String, Object> EMPTY_MAP = new HashMap<>(0);
	public MapProcessor() {
		this(ArrayProcessor.BASE_CONVERTOR);
	}
	public MapProcessor(ResultSetConvertor convertor) {
		this.convertor = convertor;
	}

	@Override
	public Map<String, Object> handle(ResultSet rs) throws SQLException {
		if(rs.next()) {
			Map<String, Object> ret = this.convertor.toMap(rs);
			if(rs.next()) throw new TooManyRecordsException();
			else return ret;
		}
		else return EMPTY_MAP;
	}
}
