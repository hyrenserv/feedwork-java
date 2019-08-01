package fd.ng.cmdtools.loadrunner;

/**
 * 任务线程基类中，创建该对象，存入相关数据
 */
public class TaskExecInfo
{
	private long timeElapse=Long.MIN_VALUE; // 用时
	private boolean failed=true; // 是否失败
	private String taskName; // 主线程传进来的任务名称
	private long threadID; // 任务线程的ID，由于使用了线程池，这个ID应该会大量重复
	private String threadName; // 任务线程的 getName()
	private String resultValue; // 子类任务处理函数的返回的数据

	public String getTaskName() {
		return taskName;
	}

	public String getResultValue() {
		return resultValue;
	}

	public void setResultValue(String resultValue) {
		this.resultValue = resultValue;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public long getThreadID() {
		return threadID;
	}

	public void setThreadID(long threadID) {
		this.threadID = threadID;
	}

	public String getErrorMessage() {
		if(errorMessage==null) return "";
		else return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private String errorMessage="";

	public long getTimeElapse() {
		return timeElapse;
	}

	public void setTimeElapse(long timeElapse) {
		this.timeElapse = timeElapse;
	}

	public boolean isFailed() {
		return failed;
	}
	public boolean isSuccess() {
		return !failed;
	}

	public void failed() {
		this.failed = true;
	}
	public void success() {
		this.failed = false;
	}
}
