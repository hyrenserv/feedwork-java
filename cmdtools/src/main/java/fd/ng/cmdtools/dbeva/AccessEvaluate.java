package fd.ng.cmdtools.dbeva;

import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.Validator;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.meta.ColumnMeta;
import fd.ng.db.meta.MetaOperator;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AccessEvaluate {
	private final String testTableName;

	public AccessEvaluate(String testTableName) {
		Validator.notEmpty(testTableName);
		this.testTableName = testTableName;
	}
	public void clear() {
		try(DatabaseWrapper db = new DatabaseWrapper();) {
			if (db.isExistTable(testTableName)) {
				db.ExecDDL("drop table " + testTableName);
				System.out.println("drop table " + testTableName);
			}
		}
	}
	public void createTestTableIfNotExists() {
		try(DatabaseWrapper db = new DatabaseWrapper();) {
			if (db.isExistTable(testTableName)) {
				System.out.printf("Table(=%s) already exists! %n", this.testTableName);
				return;
			}

			db.ExecDDL("create table " + testTableName + "(" +
						"  name varchar(48)" +
						", age int not null" +
						", create_date char(15)" +
						", money decimal(16, 2)" +
						", status char(1) default '0'" +
						")");
			if (!db.isExistTable(testTableName)) throw new RuntimeException("Table is not exist! " + testTableName);
		}
	}

	// ----------- 读数据 -------------
	/**
	 * 在做大量数据导出的时候，单条sql导出几千万级别以上数据容易造成内存溢出。
	 * 如果做成分页，可能造成越往后越慢。
	 * 需要围绕着 fetchSize ，针对不同DB做相应的处理:
	 * 大多数情况下 fetchSize 设置为 500 ，或200即可。
	 * 1） mysql
	 *      !）db url后面加上 useCursorFetch=true&defaultFetchSize=500
	 *      2）代码中 setFetchSize(Integer.MIN_VALUE);
	 *      3）要使用 5.1 以上的 jdbc jar
	 *      在 5.6 版本下，实测有效
	 * 2） postgresql
	 *      设置 autoCommit = false ，setFetchSize(500)
	 *      要使用 42.2 以上的 jdbc jar
	 *      TODO 还可以换成异步IO的jar试试看效果
	 * 3） oracle
	 *      setFetchSize(500) 可以满足大多数情况
	 *      如果要极端调优，可设置为： 4 * 1024 * 1024 / 表所有列的长度总和，会再快25%左右
	 */

	/**
	 * 分页读取
	 * @param pageNums 没页记录数
	 */
	public void read(int totalNums, int pageNums) {
		int pages = totalNums/pageNums;         // 分页数
		if( pages*pageNums<totalNums ) pages++;
		System.out.printf("Start page query : total=%d, pages=%d %n", totalNums, pages);
		String sql = "select name, age, create_date, money, status from " + testTableName;

		ExecutorService         es        = Executors.newFixedThreadPool(8); // 最多用8个线程进行并行读
		CountDownLatch          count     = new CountDownLatch(pages);
		List<Future<Integer>>   readNums  = new ArrayList<>();

		long start = System.currentTimeMillis();
		for(int i=0; i<pages; i++) {
			int begin = i*pageNums + 1;
			int end = begin + pageNums - 1;
			readNums.add(es.submit(new ReadThread(count, sql, begin, end)));
		}
		try {
			count.await();
			long end = System.currentTimeMillis();

			int readTotalNums = 0;
			for(Future<Integer> one : readNums) readTotalNums += one.get();

			System.out.printf("read data over. total read : %d, time=(%ds, %dms) %n", readTotalNums, (end-start)/1000, (end-start));
			es.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 在一个线程中读完所有数据。
	 */
	public void readOneThread() {
		System.out.printf("Start read by one thread ... %n");
		final String sql = "select * from " + testTableName;
		int colFullLen = 0; // 所有列的长度总和
		int colNums;    // 列个数
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().create()) {

			Map<String, ColumnMeta> colMeta = MetaOperator.getSqlColumnMeta(db, sql);
			for(Map.Entry<String, ColumnMeta> entry : colMeta.entrySet()) {
				ColumnMeta columnMeta = entry.getValue();
				int colLen = columnMeta.getLength();
				colFullLen += colLen;
			}

			colNums = colMeta.size(); // 列个数
			System.out.printf("%s 's column size is : %d", testTableName, colNums);
		}

		DbinfosConf.Dbinfo dbinfo = DbinfosConf.getDatabase(DbinfosConf.DEFAULT_DBNAME);
		ResultSet rs = null;
		int fetchSize = DatabaseWrapper.NULL_FETCH_SIZE;
		if(dbinfo.getDbtype()== Dbtype.ORACLE) {
			// 为了运行时能够随意切换各种情况，所以使用MIN_VALUE来决定使用那种 fetchSize
			if(dbinfo.getFetch_size()==Integer.MIN_VALUE) fetchSize = 4 * 1024 * 1024 / colFullLen;
			else fetchSize = dbinfo.getFetch_size();
		} else {
			fetchSize = dbinfo.getFetch_size();
		}
		try(DatabaseWrapper db = new DatabaseWrapper.Builder().fetchSize(fetchSize).create()) {
			long start = System.currentTimeMillis();
			rs = db.queryGetResultSet(sql);
			System.out.printf("query time : %dms %n", System.currentTimeMillis()-start);

			int nums = 0, batch = 0;
			start = System.currentTimeMillis();
			while (rs.next()) {
				String val;
				for(int i=1; i<=colNums; i++) {
					val = rs.getString(i);
				}
				nums++;
				if(nums%2000000==0) {
					batch++;
					System.out.printf("...... read %4d million %n", batch*2);
				}
			}
			long end = System.currentTimeMillis();
			System.out.printf("read data over. total read : %,d, time=(%ds, %dms) %n", nums, (end-start)/1000, (end-start));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try{if(rs!=null) rs.close();} catch (SQLException e){}
		}
	}

	public void write(final int nums, final int oneBatchNums) {
		System.out.printf("Start write ... %n");
		final int batch = nums/oneBatchNums;
		try(DatabaseWrapper db = new DatabaseWrapper();) {
			db.beginTrans();
			String sql = "insert into " + testTableName + " values(?, ?, ?, ?, ?)";

			long start = System.currentTimeMillis();
			for(int i=0; i<batch; i++) {
				db.execBatch(sql, oneBatch(i, oneBatchNums));
				db.commit();
				System.out.printf("...... write %,d %n", oneBatchNums*(i+1));
			}
			int yu = nums - batch*oneBatchNums; // 剩余的尾数
			if(yu>0) {
				db.execBatch(sql, oneBatch(batch, yu));
				db.commit();
			}
			long end = System.currentTimeMillis();

			System.out.printf("write data over. total write : %,d, time=(%ds, %dms) %n", nums, (end-start)/1000, (end-start));
		}
	}
	private List<Object[]> oneBatch(int curBatchIndex, int oneBatchNums) {
		List<Object[]> batchData = new ArrayList<>(oneBatchNums);
		String datetime = DateUtil.getDateTime();
		for(int j=0; j<oneBatchNums; j++) {
			Object[] row = new Object[]{
					"name-"+curBatchIndex+"-"+j,      // name
					curBatchIndex,                    // age
					datetime,                         // create_date
					new BigDecimal("1"+j+"."+(j%100)),    // money
					curBatchIndex%10                  // status
			};
			batchData.add(row);
		}
		return batchData;
	}
}
