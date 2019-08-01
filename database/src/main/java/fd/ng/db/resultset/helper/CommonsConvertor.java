package fd.ng.db.resultset.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 满足大多数数据转换的功能，通过实现 ResultSetConvertor 接口，继承了其默认方法
 * 从而拥有了把 ResultSet 转换为 对象、数组、列表、Map、JavaBean 等各种能力
 * 如果有对以上情况的特殊处理，重新实现 ResultSetConvertor 接口，并在 new 的时候传入即可。
 */
public class CommonsConvertor implements ResultSetConvertor {
	private static final ResultSetToBeanHelper defaultConvertor = new ResultSetToBeanHelper();
	private final ResultSetToBeanHelper convertor;

	public CommonsConvertor() { this(defaultConvertor); }
	public CommonsConvertor(ResultSetToBeanHelper convertor) {
		this.convertor = convertor;
	}

	@Override
	public <T> T toBean(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		return this.convertor.toBean(rs, classOfBean);
	}

	@Override
	public <T> List<T> toBeanList(ResultSet rs, Class<? extends T> classOfBean) throws SQLException {
		return this.convertor.toBeanList(rs, classOfBean);
	}
}
