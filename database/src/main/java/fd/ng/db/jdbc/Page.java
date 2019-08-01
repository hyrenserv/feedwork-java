package fd.ng.db.jdbc;

import fd.ng.core.exception.internal.RevitalizedCheckedException;

public interface Page
{
	int getCurrPage();

	void setCurrPage(int currPage);

	int getPageSize();

	void setPageSize(int pageSize);

	int getTotalSize();

	/**
	 * 设置查询到的总记录数。来自于 db.getCounts() 的返回值
	 * 在构造 Page 对象时，不应该调用本方法，完成查询后，会被自动设置。
	 * @param totalSize 总记录数
	 */
	void setTotalSize(int totalSize);

	int getPageCount();

	int getBeginOfPage();

	int getEndOfPage();

	void setLimit(int limit);

	/**
	 * 设置是否统计总记录数。
	 * 对于海量数据（比如上10万），如果每次都统计总记录数，会严重影响查询效率
	 * 如果业务上不需要知道确切的总记录数，可是设置为false，这样仅做分页查询，不统计总记录数
	 * @param isCountTotal true：统计， false：不统计
	 */
	void setCountTotalSize(boolean isCountTotal);

	boolean isCountTotalSize();
}
