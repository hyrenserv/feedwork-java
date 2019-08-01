package fd.ng.cmdtools.loadrunner;

import fd.ng.core.utils.ClassUtil;
import fd.ng.core.utils.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TaskLauncher {
	private int taskNums; // 执行的总次数。必须大于等于并发线程数
	private int concNums; // 并发线程数量
	// 本次压测的时间统计：总耗时，最小相应时间，最大响应时间
	private long sumRunTime;
	private String taskClassFullName; // 任务处理类名： http / jdbc / 用户自定义的类名（包括包名）

	public TaskLauncher(){
		this.sumRunTime = 0L;
		this.taskNums = 100;
		this.concNums = 10;
		this.taskClassFullName = "http";
	}
	public TaskLauncher(int taskNums, int concNums, int runingTime, String taskClassFullName, String reportType) {
		this.sumRunTime = 0;
		this.taskNums = taskNums;
		this.concNums = concNums;
		if(taskNums<concNums) this.taskNums = this.concNums;
		this.taskClassFullName = StringUtil.isBlank(taskClassFullName)?"http":taskClassFullName.trim();
	}

	/**
	 * 传入转换成Map对象的命令行参数
	 * @param cmdMap
	 */
	public List<TaskExecInfo> doRunning(final Map<String, String> cmdMap){
		if(StringUtil.isNotBlank(cmdMap.get("总执行次数")))
			this.taskNums = Integer.parseInt(cmdMap.get("总执行次数"));
		if(StringUtil.isNotBlank(cmdMap.get("并发数")))
			this.concNums =	Integer.parseInt(cmdMap.get("并发数"));
		if(this.taskNums<this.concNums) this.taskNums = this.concNums;
		this.taskClassFullName = StringUtil.isBlank(cmdMap.get("任务处理类"))?"http":cmdMap.get("任务处理类").trim();

		// 处理任务处理类
		if( StringUtil.isBlank(this.taskClassFullName)|| "http".equalsIgnoreCase(this.taskClassFullName) )
		{ // 使用系统内置的任务处理类
			this.taskClassFullName = "fd.ng.cmdtools.loadrunner.HttpTaskThread";
			System.out.println("Used default Task Class <HttpTaskThread> for current testing.");
		} else if( "jdbc".equalsIgnoreCase(this.taskClassFullName) ) {
			this.taskClassFullName = "fd.ng.cmdtools.loadrunner.JdbcTaskThread";
			System.out.println("Used default Task Class <JdbcTaskThread> for current testing.");
		} else { // 用户提供自定义的任务处理类
			System.out.println("Used user-defined Task Class <"+this.taskClassFullName+"> for current testing.");
		}

		// 记录所有任务线程是否都执行完成
		final CountDownLatch latch = new CountDownLatch(this.taskNums);
		// 按照并发数，创建相应数量的线程池。
		ExecutorService threadPool = Executors.newFixedThreadPool(this.concNums);

		// 构建传递给任务线程的全局数据
		Map<String, Object> forThreadData = null;
		// 构造任务线程对象
		List<TaskPanel> taskThreadObjectList = new ArrayList<>();
		try {
			Class<?> clsTask = ClassUtil.loadClass(taskClassFullName);
			Method stcMethod = clsTask.getMethod("buildInitTaskData", Map.class);
			forThreadData = (Map<String, Object>)stcMethod.invoke(null, cmdMap);
			for(int i = 0; i < this.taskNums; i++) {
				Constructor conrTask = clsTask.getDeclaredConstructor(String.class, Map.class);
				conrTask.setAccessible(true);
				Object task = conrTask.newInstance("task-"+i, forThreadData);
				Method methodSetLatch = clsTask.getMethod("setCountDownLatch", CountDownLatch.class);
				methodSetLatch.invoke(task, latch);
				taskThreadObjectList.add((TaskPanel)task);
			}
		} catch (Exception e) {
			throw new RuntimeException("Can not load class : " + taskClassFullName, e);
		}


		// 开始持续施压
		CompletionService<TaskExecInfo> taskCompletionService = new ExecutorCompletionService<TaskExecInfo>(threadPool);
		this.sumRunTime = 0;
		long startMainThread = System.currentTimeMillis();
		for(TaskPanel taskThreadObject : taskThreadObjectList) {
			taskCompletionService.submit(taskThreadObject);
		}
		// 使用CompletionService，将得到一个按照完成先后顺序的任务线程执行结果
		List<TaskExecInfo> taskExecInfoList = new ArrayList<>();
		for(int i = 0; i < this.taskNums; i++) {
			try {
				Future<TaskExecInfo> result = taskCompletionService.take();
				TaskExecInfo taskExecInfo = result.get();
				taskExecInfoList.add(taskExecInfo);
			} catch (InterruptedException e) {
				System.out.println("the ["+(i+1)+"] thread InterruptedException");
				e.printStackTrace(System.out);
			} catch (ExecutionException e) {
				System.out.println("the ["+(i+1)+"] thread ExecutionException");
				e.printStackTrace(System.out);
			}
		}
		long endMainThread = System.currentTimeMillis();
		// 计算总耗时
		this.sumRunTime = (endMainThread-startMainThread);
		System.out.println("Running task thread count : " + latch.getCount());
		threadPool.shutdown();
		return taskExecInfoList;
	}

	public int getTaskNums() {
		return taskNums;
	}

	public int getConcNums() {
		return concNums;
	}

	public long getSumRunTime() {
		return sumRunTime;
	}
}
