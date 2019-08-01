package fd.ng.db.jdbc.nature;

import fd.ng.core.exception.internal.FrameworkRuntimeException;

public class MySQL extends AbstractNatureDatabase {
	private MySQL() {}

	public static PagedSqlInfo toPagedSql(String sql, int orginBegin, int orginEnd) {
		if(orginBegin<1) throw new FrameworkRuntimeException("page begin must greater than 0");
		PagedSqlInfo pagedSqlInfo = new PagedSqlInfo();

		if(orginBegin>1) {
			pagedSqlInfo.setSql(sql + " limit ?, ?");
			pagedSqlInfo.setPageNo1(orginBegin - 1);
			pagedSqlInfo.setPageNo2(orginEnd - orginBegin + 1);
		} else {
			pagedSqlInfo.setSql(sql + " limit ?");
			pagedSqlInfo.setPageNo1(orginEnd);
			pagedSqlInfo.setPageNo2(PagedSqlInfo.PageNoValue_NotExist); // 该参数已经无意义
		}
		return pagedSqlInfo;
	}
}
