package fd.ng.db.util;

import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.BeanUtil;

import java.util.List;

public class SqlUtil {
	/**
	 * 把带有 ? 占位符的SQL，及对应的占位参数，拼接成日志中显示的可读格式。
	 *
	 * @param sql     把带有 ? 占位符的SQL
	 * @param params  对应的占位参数
	 * @return 格式良好的SQL
	 */
	public static String getGoodshowSql(String sql, Object... params) {
		return getGoodshowSql(-1, -1, sql, params);
	}

	/**
	 * 把带有 ? 占位符的SQL，及对应的占位参数，拼接成日志中显示的可读格式。
	 * 面向分页SQL。
	 *
	 * @param begin   开始页。如果不是分页，传入-1，用于内部逻辑判断是否有这个参数
	 * @param end     结束页。如果不是分页，传入-1，用于内部逻辑判断是否有这个参数
	 * @param sql     把带有 ? 占位符的SQL
	 * @param params  对应的占位参数
	 * @return 格式良好的SQL
	 */
	public static String getGoodshowSql(int begin, int end, String sql, Object... params) {
		int i=0,loc=0;
		int paramSize = params.length;
		if ( paramSize==1 ) {
			if( params[0] instanceof List) {
				List paramList = ((List) params[0]);
				paramSize = paramList.size();
				params = paramList.toArray(new Object[paramSize]);
			}
		}
		while(loc>-1){
			loc = sql.indexOf('?');
			if(loc<0) return sql;
			if(i>=paramSize){//即将要从params中取的数据已经超出了params中数据的个数。因为params是从0开始取数，所以这里是>=
				if(begin==-1) // 无分页
					throw new RawlayerRuntimeException("SQL=["+sql+"] error. more '?' than param.");
				else {
					sql = sql.replaceFirst("\\?", String.valueOf(begin));
					sql = sql.replaceFirst("\\?", String.valueOf(end));
					if(sql.contains("?"))
						throw new RawlayerRuntimeException("Paged SQL=["+sql+"] error. more '?' than param.");
					else
						return sql;
				}
			}
			Object param=params[i++];
			if(param==null) {
				sql=sql.replaceFirst("\\?","null");
			} else if (BeanUtil.isNumberClass(param)){
				sql=sql.replaceFirst("\\?",param.toString());
			} else if(param instanceof String){
				String tmp_param = (String) param;
				tmp_param = tmp_param.replaceAll("\\?", "？");
				sql=sql.replaceFirst("\\?","'"+tmp_param+"'");
			} else{
				sql=sql.replaceFirst("\\?","'"+param.toString()+"'");
			}
		}
		return sql;
	}

	private SqlUtil() {}
}
