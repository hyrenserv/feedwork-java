package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

public class Oracle9iAbove extends AbstractNatureDatabase {
	private Oracle9iAbove(){}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		sql = sql.trim();
		boolean hasForUpdate = false;
		if (sql.toLowerCase().endsWith(ForUpdate)) {
			sql = sql.substring( 0, sql.length() - 11 );
			hasForUpdate = true;
		}

		final StringBuilder _pagedSql = new StringBuilder( sql.length() + 100 );
		if(orginBegin>1) {
			_pagedSql.append("select * from ( select row_.*, rownum rownum_ from ( ");
		} else {
			_pagedSql.append("select * from ( ");
		}
		_pagedSql.append(sql);
		if(orginBegin>1) {
			_pagedSql.append(" ) row_ where rownum <= ?) where rownum_ > ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo1(orginBegin);
		}
		else {
			_pagedSql.append(" ) where rownum <= ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist); // 该参数已经无意义
		}
		if (hasForUpdate) {
			_pagedSql.append(ForUpdate);
		}
		pagedSqlInfo.setSql(_pagedSql.toString());
		return pagedSqlInfo;
	}
}
