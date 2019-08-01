package fd.ng.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StopWatch {
	private static final Logger logger = LogManager.getLogger(StopWatch.class.getName());

	private String name;
	private long startTimeMillis; // 开始时间，单位毫秒
	private long endTimeMillis; // 结束时间，单位毫秒
	private long totalTimeMillis; // 总体运行时间，单位毫秒

	public StopWatch() {
		this(null);
	}
	public StopWatch(String name) {
		this.name = name;
		this.totalTimeMillis = -1;
	}

	public void start() {
		this.startTimeMillis = System.currentTimeMillis();
		this.endTimeMillis = -1;
		this.totalTimeMillis = -1;
	}
	public void start(String name) {
		this.name = name;
		start();
	}

	public void stop() {
		this.endTimeMillis = System.currentTimeMillis();
		this.totalTimeMillis = this.endTimeMillis - this.startTimeMillis;
	}

	public void stopShow() {
		stop();
		String taskFlag = this.name;
		if(taskFlag==null) taskFlag = "";
		logger.debug("{} took time : {}s, {}ms.",
				taskFlag, (this.totalTimeMillis/1000), this.totalTimeMillis);
	}

	public void stopShowSystemOut() {
		stop();
		String taskFlag = this.name;
		if(taskFlag==null) taskFlag = "";
		System.out.printf("%s took time : %ds, %dms.",
				taskFlag, (this.totalTimeMillis/1000), this.totalTimeMillis);
	}

	public long getStartTime() { return this.startTimeMillis; }
	public long getEndTime() { return this.endTimeMillis; }
	public long getTotalTime() { return this.totalTimeMillis; }
}
