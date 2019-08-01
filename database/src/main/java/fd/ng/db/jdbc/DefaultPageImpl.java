package fd.ng.db.jdbc;

/**
 * 默认实现类
 */
public class DefaultPageImpl implements Page
{
	public static final int DEFAULT_PAGE_SIZE = 10;
	protected boolean countTotalSize;
	/**总记录数*/
	protected int totalSize;
	/**每页记录数*/
	protected int pageSize = DefaultPageImpl.DEFAULT_PAGE_SIZE;
	/**当前页号,缺省为第一页*/
	protected int currPage;
	protected int limit;

	public DefaultPageImpl() {
		this.currPage       = 1;
		this.countTotalSize = true;
	}
	public DefaultPageImpl(int currPage, int pageSize) {
		this.currPage       = currPage;
		this.pageSize       = pageSize;
		this.countTotalSize = true;
	}
	public DefaultPageImpl(int currPage, int pageSize, boolean countTotalSize) {
		this.currPage       = currPage;
		this.pageSize       = pageSize;
		this.countTotalSize = countTotalSize;
	}
	public DefaultPageImpl(int limit) {
		this.limit          = limit;
		// 使用limit，相当于取第1页且PageSize等于limit值
		this.currPage       = 1;
		this.pageSize       = limit;
		this.countTotalSize = true;
	}
	public DefaultPageImpl(int limit, boolean countTotalSize) {
		this.limit          = limit;
		// 使用limit，相当于取第1页且PageSize等于limit值
		this.currPage       = 1;
		this.pageSize       = limit;
		this.countTotalSize = countTotalSize;
	}

	@Override
	public int getBeginOfPage()
	{
		return currPage>1?(currPage-1)*pageSize+1:1;
	}

	@Override
	public int getCurrPage()
	{
		return currPage;
	}

	@Override
	public int getEndOfPage()
	{
		return getBeginOfPage() + getPageSize()-1;
	}

	@Override
	public int getPageCount()
	{
		return (totalSize + pageSize -1) / pageSize;
	}

	@Override
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public int getTotalSize()
	{
		return totalSize;
	}

	@Override
	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	@Override
	public void setLimit(int limit) {
		this.limit    = limit;
		// 使用limit，相当于取第1页且PageSize等于limit值
		this.currPage = 1;
		this.pageSize = limit;
	}

	@Override
	public void setCurrPage(int currPage)
	{
		this.currPage = currPage;
	}

	@Override
	public void setTotalSize(int totalSize)
	{
		this.totalSize = totalSize;
	}

	@Override
	public void setCountTotalSize(boolean isCountTotal) {
		this.countTotalSize = isCountTotal;
	}

	@Override
	public boolean isCountTotalSize() { return countTotalSize; }
}
