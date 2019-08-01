package fd.ng.cmdtools.loadrunner;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public abstract class TaskPanel implements Callable<TaskExecInfo> {
	protected final String taskName; // 主线程给每个任务起的唯一名字
	protected final Map<String, Object> forThreadData; // 主线程构造的对象，存储所有线程共享需要的数据
	protected CountDownLatch m_latch = null;

	public TaskPanel(String taskName, Map<String, Object> forThreadData){
		this.taskName = taskName;
		this.forThreadData = forThreadData;
	}

	/**
	 * 必须在启动线程前调用本函数！！！
	 * @param latch
	 */
	public void setCountDownLatch(CountDownLatch latch){
		this.m_latch = latch;
	}
	@Override
	public TaskExecInfo call() throws Exception {
		TaskExecInfo taskInfo = new TaskExecInfo();
		taskInfo.setTaskName(this.taskName);
		taskInfo.setThreadID(Thread.currentThread().getId());
		taskInfo.setThreadName(Thread.currentThread().getName());
		String taskResultValue = null;
		long start=0, end=0; // 用于计算该线程任务的执行耗时
		start = System.currentTimeMillis();
		try {
			taskResultValue = executor();
			end = System.currentTimeMillis();
			//JsonUtil.toObject(taskResultValue, TypeToken);
			taskInfo.success();
		} catch (TaskException e) {
			end = System.currentTimeMillis();
			taskInfo.failed();
			taskInfo.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			end = System.currentTimeMillis();
			taskInfo.failed();
			taskInfo.setErrorMessage(e.toString());
		}
		taskInfo.setTimeElapse((end-start));
		taskInfo.setResultValue(taskResultValue);
		this.m_latch.countDown();

		return taskInfo;
	}

	/**
	 * 子类实现该方法，完成实际的任务处理逻辑。
	 *
	 * @return 任务处理结果。在主线程中，为成每个任务线程生成结果报告时，显示在报告中。
	 * @throws TaskException 子线程中，对各种错误包装成该异常抛出
	 */
	protected abstract String executor() throws TaskException;
}
