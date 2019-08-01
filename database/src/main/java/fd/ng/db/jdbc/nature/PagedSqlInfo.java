package fd.ng.db.jdbc.nature;

/**
 * 构造真实数据库的分页数据
 */
public class PagedSqlInfo {
	public static final int PageNoValue_NotExist = Integer.MIN_VALUE;

	private String sql;      // 转换后的SQL，多了用于分页的占位 '?'
	private int    pageNo1;  // 对应第1个占位 '?' 的值。必然大于0
	private int    pageNo2;  // 对应第2个占位 '?' 的值。对于去前N条记录的情况，该参数值无意义，等于PageNoValue_NotExist

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public int getPageNo1() {
		return pageNo1;
	}

	public void setPageNo1(final int pageNo1) {
		this.pageNo1 = pageNo1;
	}

	public int getPageNo2() {
		return pageNo2;
	}

	public void setPageNo2(final int pageNo2) {
		this.pageNo2 = pageNo2;
	}
}
