package fd.ng.cmdtools.dbeva;

import fd.ng.db.jdbc.DatabaseWrapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ReadThread implements Callable<Integer> {
	private final CountDownLatch count;
	private final String querySql;
	private final int pageBegin;
	private final int pageEnd;
	private final String thrCode;
	public ReadThread(final CountDownLatch count, final String querySql, final int begin, final int end) {
		this.count      = count;
		this.querySql   = querySql;
		this.pageBegin  = begin;
		this.pageEnd    = end;
		this.thrCode    = "["+begin+"..."+end+"]";
	}
	@Override
	public Integer call() throws Exception {
		ResultSet rs = null;
		try(DatabaseWrapper db = new DatabaseWrapper();) {
			System.out.printf("%s start reading. %n", thrCode);
			long start = System.currentTimeMillis();
			rs = db.queryPagedGetResultSet(querySql, pageBegin, pageEnd, false);
			long end = System.currentTimeMillis();
			System.out.printf("query %s time=(%ds, %dms) %n", thrCode, (end-start)/1000, (end-start));

			int nums = 0;
			start = System.currentTimeMillis();
			while (rs.next()) {
				String name         = rs.getString(1);
				int    age          = rs.getInt(2);
				Object dt           = rs.getObject(3);
				BigDecimal money    = rs.getBigDecimal(4);
				String status       = rs.getString(5);
				nums++;
			}
			end = System.currentTimeMillis();
			System.out.printf("read %s, total read : %d, time=(%ds, %dms) %n", thrCode, nums, (end-start)/1000, (end-start));
			return nums;
		} catch (SQLException e) {
			throw new RuntimeException(thrCode, e);
		} finally {
			try{if(rs!=null) rs.close();} catch (SQLException e){}
			count.countDown();
		}
	}
}
