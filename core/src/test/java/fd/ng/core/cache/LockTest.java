package fd.ng.core.cache;

import fd.ng.core.cache.bean.ForReadWriteBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试读写锁
 */
public class LockTest {
	private ForReadWriteBean objectSync = new ForReadWriteBean();
	private int valSync;
	private static final MuchReadFewWriteCache<String, Object> objCache = new MuchReadFewWriteCache<>();

	public final static int READ_NUM = 180; // 读线程数
	public final static int WRITE_NUM = 20; // 写线程数

	public static void main(String[] args) throws Exception {
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(READ_NUM+WRITE_NUM);
		final CountDownLatch latch = new CountDownLatch(READ_NUM + WRITE_NUM); // 线程计数，用于关闭线程池判断
		final CountDownLatch start = new CountDownLatch(1); // 线程计数，用于关闭线程池判断

		for (int i = 0; i < READ_NUM; i++) {
			final int curIndex = i;
			newFixedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
					} catch (Exception e) {
						e.printStackTrace();
					}

					String objName = "obj-"+(curIndex%WRITE_NUM);

					long start = System.nanoTime();
					Object obj = objCache.get(objName);
					long end = System.nanoTime();
					System.out.printf("Read  [ %s ] tims=%10d | %s %n", objName, (end-start), obj);

					latch.countDown();
				}
			});
		}

		for (int i = 0; i < WRITE_NUM; i++) {
			final int curIndex = i;
			newFixedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
					} catch (Exception e) {
						e.printStackTrace();
					}

					String objName = "obj-"+curIndex;
					ForReadWriteBean obj = new ForReadWriteBean();
					obj.setName(objName);
					obj.setAge(curIndex);

					long start = System.nanoTime();
					objCache.put(objName, obj);
					System.out.printf("Write [ %s ] tims=%10d %n", objName, (System.nanoTime()-start));

					latch.countDown();
				}
			});
		}

		// 同时启动
		start.countDown();

		try {
			latch.await();
			newFixedThreadPool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
