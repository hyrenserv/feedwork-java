package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

public class PostgreSQL extends AbstractNatureDatabase {
	private PostgreSQL() {}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		if(orginBegin>1) {
			pagedSqlInfo.setSql(sql + " limit ? offset ?");
			pagedSqlInfo.setPageNo1(orginEnd - orginBegin + 1);
			pagedSqlInfo.setPageNo2(orginBegin - 1); // 因为 pg 是从0开始计算记录位置
		} else {
			pagedSqlInfo.setSql(sql + " limit ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist);
		}
		return pagedSqlInfo;
	}
}
