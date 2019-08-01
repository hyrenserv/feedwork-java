package fd.ng.cmdtools.loadrunner;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.UuidUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * TODO 自定义类出现空指针（没启动TOMCAT）；ConcurrentHashMap改static？
 */
public class LoadRunnerMain
{
	private Map<String, String> _cmdMap;

	public void start() {
		System.out.printf("%n>>>>>>>>>>>>> 压力测试开始 ... ... %n%n");

		TaskLauncher taskLauncher = new TaskLauncher();
		final int runningTime = Integer.parseInt(_cmdMap.getOrDefault("施压持续时间", "0")); // 单位：秒
		if (runningTime<0) throw new RuntimeException("施压持续时间(-w) 必须大于0！");
		final long endTime = System.currentTimeMillis() + runningTime*1000 - 1; // 减1是为了当runningTime为0时，保证能退出下面循环
		while (true) {
			List<TaskExecInfo> taskExecInfoList = taskLauncher.doRunning(_cmdMap);

			// 排序，方便查看（可选：服务端处理时间、任务名、线程ID）
			String sort = _cmdMap.getOrDefault("排序列", "serverTime");
			if ("serverTime".equalsIgnoreCase(sort))
				taskExecInfoList.sort(
						(item1, item2) -> new Long(item1.getTimeElapse()).compareTo(new Long(item2.getTimeElapse()))
				);
			else if ("taskName".equalsIgnoreCase(sort))
				taskExecInfoList.sort(
						(item1, item2) -> item1.getTaskName().compareTo(item2.getTaskName())
				);
			else if ("threadId".equalsIgnoreCase(sort))
				taskExecInfoList.sort(
						(item1, item2) -> new Long(item1.getThreadID()).compareTo(new Long(item2.getThreadID()))
				);
			String reportType = StringUtil.isBlank(_cmdMap.get("结果报告类型")) ? "console" : _cmdMap.get("结果报告类型").trim();
			if ("csv".equals(reportType)) {
				outReportCsv(taskLauncher, taskExecInfoList);
			} else {
				outReportConsole(taskLauncher, taskExecInfoList);
			}

			if ( System.currentTimeMillis()>endTime ) break;
			try { Thread.sleep(33); } catch (InterruptedException e) {}
		}
		System.out.printf("%n>>>>>>>>>>>>> 压力测试结束 %n%n");
	}

	public LoadRunnerMain(String[] args) {
		buildCMDArgs(args);
	}

	public Map<String, String> getCmdMap() {
		return _cmdMap;
	}

	/**
	 * 计算均值、中位数、众数、最大值、最小值
	 * @param taskInfoList
	 * @return
	 */
	private Map<String, Long> calculate(final List<TaskExecInfo> taskInfoList){
		if(taskInfoList.size()<1) return Collections.emptyMap();

		int len = taskInfoList.size();
		// 存储每个任务的服务器处理时间。用于计算中位数、众数用
		List<Long> timeElapseList = new ArrayList<>(len);

		/* 1. 计算总耗时、最大处理时间、最小处理时间 */
		long minTime = Integer.MAX_VALUE;
		long maxTime = Integer.MIN_VALUE;
		long sumTime = 0; // 总耗时
		for (int i=0; i<len; i++) {
			TaskExecInfo taskInfo = taskInfoList.get(i);
			long curElapseTime = taskInfo.getTimeElapse();
			if (curElapseTime > maxTime) maxTime = curElapseTime;
			if (curElapseTime < minTime) minTime = curElapseTime;
			sumTime += curElapseTime;
			timeElapseList.add(curElapseTime);
		}

		/* 2. 计算处理时间的中位数 */
		Collections.sort(timeElapseList);
		long midTime = -1;
		if(len==1) midTime = timeElapseList.get(0);
		else if(len==2) midTime = timeElapseList.get(1);
		else if(len%2==0) midTime = (timeElapseList.get(len/2)+timeElapseList.get(len/2+1))/2;
		else midTime = timeElapseList.get(len/2);

		/* 3. 计算处理时间的众数 */
		// 存储每个耗时的出现频率。key是耗时数，value是频率
		Map<Long, Long> freqMap = new HashMap<>();
		// 遍历统计各个耗时的频率
		timeElapseList.forEach(item->{
			freqMap.put(item, freqMap.getOrDefault(item, 0l) + 1);
		});

		// 存储统计结果的map：key=monum，value=众数； key=count，value=众数的个数
		Map<String, Long> res = new HashMap<>(2);
		res.put("monum", Long.MIN_VALUE); res.put("count", -1l);
		// 遍历，找众数
		freqMap.forEach((k,v)->{
			long lastVal = res.get("count"); // 上次循环找到的最大频率
			if(v>lastVal) {
				res.put("monum", k); // 新出现的这个最大数字
				res.put("count", v); // 新出现的这个最大数字的频率
			}
		});

		/* 保存所有统计结果并返回 */
		Map<String, Long> data = new HashMap<>();
		data.put("minTime", minTime);
		data.put("maxTime", maxTime);
		data.put("midTime", midTime);
		data.put("moTime", res.get("monum"));
		data.put("moCount", res.get("count"));
		data.put("avgTime", sumTime/len);
		return data;
	}

	/**
	 * 分析各个处理时间的分布区间。
	 * 按照：[ minTime -> 1/2 -> midTime -> 3/4 -> maxTime ] 四个区间统计各个处理时间
	 */
	private RangeMap<Long, Integer> analysTimeScatter(final List<TaskExecInfo> taskInfoList,
															 long minTime, long maxTime, long midTime) {
		// 初始设置好需要的4个区间
		long r1Top = (midTime-minTime)/2 + minTime; // 中位数-最小时间 的一半
		long r2Top = midTime; // 中位数
		long r3Top = (maxTime-midTime)/2 + midTime; // 最大时间-中位数 的一半
		Range<Long> range1 = Range.closed(minTime, r1Top);
		Range<Long> range2 = Range.openClosed(r1Top, r2Top);
		Range<Long> range3 = Range.openClosed(r2Top, r3Top);
		Range<Long> range4 = Range.openClosed(r3Top, maxTime);

		// 开始统计计算每个区间内的耗时分布情况
		RangeMap<Long, Integer> rangeMap = TreeRangeMap.create();
		for(TaskExecInfo taskExecInfo : taskInfoList) {
			long keyVal = taskExecInfo.getTimeElapse(); // 服务端的处理时间
			Integer existVal = rangeMap.get(keyVal);
			if(keyVal>=minTime && keyVal<=r1Top) {
				if(existVal==null) rangeMap.put(range1, 1);
				else rangeMap.put(range1, existVal+1);
			} else if(keyVal>r1Top && keyVal<=r2Top) {
				if(existVal==null) rangeMap.put(range2, 1);
				else rangeMap.put(range2, existVal+1);
			} else if(keyVal>r2Top && keyVal<=r3Top) {
				if(existVal==null) rangeMap.put(range3, 1);
				else rangeMap.put(range3, existVal+1);
			} else if(keyVal>r3Top && keyVal<=maxTime) {
				if(existVal==null) rangeMap.put(range4, 1);
				else rangeMap.put(range4, existVal+1);
			} else {
				System.out.println("out range time value : " + keyVal + ", in task : " + taskExecInfo.getTaskName());
			}
		}

		return rangeMap;
	}

	/**
	 * 在屏幕上显示压测结果
	 * @param taskLauncher
	 * @param taskInfoList
	 */
	private void outReportConsole(TaskLauncher taskLauncher, List<TaskExecInfo> taskInfoList){
		System.out.println();

		int failedNums = 0;
		if(!"true".equalsIgnoreCase(_cmdMap.get("关闭明细清单"))) {
			System.out.println("每个压测线程的运行情况清单：");
			System.out.printf("| %-10s | %-5s | %-18s | %-6s| %-50s | %s\n"
					, "Task Name", "Th ID", "Thread Name", "Time", "Res Value", "Fail?");
			for (TaskExecInfo taskInfo : taskInfoList) {
				String resVal = taskInfo.getResultValue();
				if (resVal == null) resVal = "No response data";
				else resVal = resVal.trim().replaceAll("\n", "");
				if (resVal.length() > 45) resVal = resVal.substring(0, 45) + " ...";
				System.out.printf("| %-10s | %-5d | %-18s | %-5d | %-50s | %s\n"
						, taskInfo.getTaskName()
						, taskInfo.getThreadID()
						, taskInfo.getThreadName()
						, taskInfo.getTimeElapse()
						, resVal
						, taskInfo.isFailed() ? "{Failed : " + taskInfo.getErrorMessage() + "}" : ""
				);
				if (taskInfo.isFailed()) failedNums++;
			}
		} else {
			for (TaskExecInfo taskInfo : taskInfoList) {
				if (taskInfo.isFailed()) failedNums++;
			}
		}

		// 得到最大值、最小值、平均值、中位数、众数
		Map<String, Long> dataAnylics = calculate(taskInfoList);
		// 按4个区间得到耗时分布情况
		RangeMap<Long, Integer> rangeMap = analysTimeScatter(taskInfoList,
				dataAnylics.get("minTime"), dataAnylics.get("maxTime"), dataAnylics.get("midTime"));

		System.out.println("\n*************** Testing Report ****************");
		System.out.printf("*  %-20s : %,-20d*   --> 本次压测的总执行次数%n", "Total nums", taskLauncher.getTaskNums());
		System.out.printf("*  %-20s : %,-20d*   --> 本次压测的并发数%n", "Concurrency nums", taskLauncher.getConcNums());
		System.out.printf("*  %-20s : %,-20d*   --> 总耗时(毫秒)%n", "Total running time", taskLauncher.getSumRunTime());
		System.out.printf("*  %-20s : %,-20d*   --> 失败的次数%n", "Failed nums", failedNums);
		System.out.printf("*%46s%n", "*");
		System.out.printf("*  %-20s : %,-20d*   --> 最小响应时间(毫秒)%n", "Min response time", dataAnylics.get("minTime"));
		System.out.printf("*  %-20s : %,-20d*   --> 最大响应时间(毫秒)%n", "Max response time", dataAnylics.get("maxTime"));
		System.out.printf("*  %-20s : %,-20d*   --> 平均响应时间(毫秒)%n", "Avg response time", dataAnylics.get("avgTime"));
		System.out.printf("*  %-20s : %,-20d*   --> 响应时间中位数(毫秒)%n", "Mid response time", dataAnylics.get("midTime"));
		System.out.printf("*  %-20s : %,-8d%-12d*   --> 响应时间众数及数量%n", "Mod response info", dataAnylics.get("moTime"), dataAnylics.get("moCount"));
		System.out.printf("*  %-20s : %20s*   --> 相应时间分布情况%n", "Range response time", " ");
		for(Map.Entry<Range<Long>, Integer> entry : rangeMap.asMapOfRanges().entrySet()) {
			System.out.printf("*  %20s %-17s %-4d* %n", " ", entry.getKey(), entry.getValue());
		}
		System.out.println(String.format("%47s", "*").replace(' ', '*'));
	}

	private void outReportCsv(TaskLauncher taskLauncher, List<TaskExecInfo> taskInfoList) {
		System.out.println();

		int failedNums = 0;
		StringBuilder csvContentBuf = new StringBuilder(5000);
		if(!"true".equalsIgnoreCase(_cmdMap.get("关闭明细清单"))) {
			csvContentBuf.append(makeCsvColumn("Task_Name")).append(',')
					.append(makeCsvColumn("Thread_ID")).append(',')
					.append(makeCsvColumn("Thread_Name")).append(',')
					.append(makeCsvColumn("Time")).append(',')
					.append(makeCsvColumn("Response_Value")).append(',')
					.append(makeCsvColumn("Result_Flag")).append(',')
					.append(makeCsvColumn("Fail_Message")).append(String.format("%n"));
			for (TaskExecInfo taskInfo : taskInfoList) {
				String resVal = taskInfo.getResultValue();
				if (resVal == null) resVal = "[ Null response value ]";
				csvContentBuf.append(makeCsvColumn(taskInfo.getTaskName())).append(',')
						.append(makeCsvColumn(Long.toString(taskInfo.getThreadID()))).append(',')
						.append(makeCsvColumn(taskInfo.getThreadName())).append(',')
						.append(makeCsvColumn(Long.toString(taskInfo.getTimeElapse()))).append(',')
						.append(makeCsvColumn(resVal)).append(',')
						.append(makeCsvColumn(Boolean.toString(taskInfo.isSuccess()))).append(',')
						.append(makeCsvColumn(taskInfo.getErrorMessage())).append(String.format("%n"));
				if (taskInfo.isFailed()) failedNums++;
			}
			csvContentBuf.append(String.format("%n"));
		} else {
			for (TaskExecInfo taskInfo : taskInfoList) {
				if (taskInfo.isFailed()) failedNums++;
			}
		}

		// 得到最大值、最小值、平均值、中位数、众数
		Map<String, Long> dataAnylics = calculate(taskInfoList);
		// 按4个区间得到耗时分布情况
		RangeMap<Long, Integer> rangeMap = analysTimeScatter(taskInfoList,
				dataAnylics.get("minTime"), dataAnylics.get("maxTime"), dataAnylics.get("midTime"));

		csvContentBuf.append("\"************** Testing Report **************\"").append(String.format("%n"))
				.append(String.format("\"*  %-20s : %,-20d*   --> 本次压测的总执行次数\"%n", "Total nums", taskLauncher.getTaskNums()))
				.append(String.format("\"*  %-20s : %,-20d*   --> 本次压测的并发数\"%n", "Concurrency nums", taskLauncher.getConcNums()))
				.append(String.format("\"*  %-20s : %,-20d*   --> 总耗时(毫秒)\"%n", "Total running time", taskLauncher.getSumRunTime()))
				.append(String.format("\"*  %-20s : %,-20d*   --> 失败的次数\"%n", "Failed nums", failedNums))
				.append(String.format("\"*%46s\"%n", "*"))
				.append(String.format("\"*  %-20s : %,-20d*   --> 最小响应时间(毫秒)\"%n", "Min response time", dataAnylics.get("minTime")))
				.append(String.format("\"*  %-20s : %,-20d*   --> 最大响应时间(毫秒)\"%n", "Max response time", dataAnylics.get("maxTime")))
				.append(String.format("\"*  %-20s : %,-20d*   --> 平均响应时间(毫秒)\"%n", "Avg response time", dataAnylics.get("avgTime")))
				.append(String.format("\"*  %-20s : %,-20d*   --> 响应时间中位数(毫秒)\"%n", "Mid response time", dataAnylics.get("midTime")))
				.append(String.format("\"*  %-20s : %,-8d%-12d*   --> 响应时间众数及数量\"%n", "Mod response info", dataAnylics.get("moTime"), dataAnylics.get("moCount")))
				.append(String.format("\"*  %-20s : %20s*   --> 相应时间分布情况\"%n", "Range response time", " "));
		for(Map.Entry<Range<Long>, Integer> entry : rangeMap.asMapOfRanges().entrySet()) {
			csvContentBuf.append(String.format("\"*  %20s %-17s %-4d* \"%n", " ", entry.getKey(), entry.getValue()));
		}
		csvContentBuf.append(String.format("\"%47s\"", "*").replace(' ', '*'));
//		FileWriter fw = null;
//		PrintWriter out = null;
		String fileName = "./Testing_Report_"+ System.currentTimeMillis() +".csv";
		FileUtil.createOrReplaceFile(fileName, csvContentBuf.toString(), CodecUtil.GBK_CHARSET);
		System.out.println("测试报告已经生成：" + Paths.get(fileName).toAbsolutePath().toString());
	}

	private String makeCsvColumn(String str){
		return "\"" + str.replace("\"", "\"\"") + "\"";
	}

	/**
	 * 检查命令行参数，并解析到Map中返回
	 * @param args
	 * @return
	 */
	private Map<String, String> buildCMDArgs(String[] args){
		String usage = String.format("%n%42s%n", "*").replace(' ', '*');
		usage += String.format("*%n");
		usage += String.format("*  loadrunner OPTIONS :%n");
//		usage += String.format("*  java -jar jarfilename [OPTIONS]%n");
//		usage += String.format("*%n");
//		usage += String.format("*  OPTIONS: %n");
		usage += String.format("*       -n100 default : 100 压测的总执行次数 %n");
		usage += String.format("*       -c10  default : 10 压测的并发数 %n");
		usage += String.format("*       -w100 压测的总执行时间。单位是秒。默认不启用这个参数 %n");
		usage += String.format("*       class=http/jdbc/yours class fullname default http 压测任务处理类 %n");
		usage += String.format("*       report=console/csv 默认： console 压测结果生成方式 屏幕输出或生成CSV文件 %n");
		usage += String.format("*       sort=serverTime/taskName/threadId 默认： servertime 明细清单的排序列：服务端处理时间、任务名称、任务线程ID %n");
		usage += String.format("*       -d0 关闭明细清单。当-n设置的很大时，如果是屏幕输出，可以启用本开关。 %n");
		usage += String.format("*       用户可传入更多自定义参数，格式为：name 或 name=value %n");
		usage += String.format("*%n");
		usage += String.format("%42s%n%n", "*").replace(' ', '*');

		/**
		 * 用户自定义的命令行参数必须是name=value的形式
		 * 从第一个 = 开始分割成 Map<String, String> 格式存储，
		 * 传给 任务线程类的 buildInitTaskData 函数，用于构建自己的全局对象。
		 */
		if(args.length<1){
			System.out.println("\n" + usage);
			System.exit(1);
		}
		_cmdMap = new HashMap<>();
		for(String arg : args){
			if(arg.length()<3){
				System.out.printf("Ignored cmd argument : [%s]\n", arg);
				continue;
			}
			if(arg.startsWith("-n")){
				String n = arg.substring(2);
				_cmdMap.put("总执行次数", n);
			} else if(arg.startsWith("-w")){
				String w = arg.substring(2);
				_cmdMap.put("施压持续时间", w);
			} else if(arg.startsWith("-c")){
				String c = arg.substring(2);
				_cmdMap.put("并发数", c);
			} else if(arg.startsWith("class=")){ // 压测的任务处理类
				String clsName = arg.substring(6);
				_cmdMap.put("任务处理类", clsName);
			} else if(arg.startsWith("report=")){
				String report = arg.substring(7);
				_cmdMap.put("结果报告类型", report);
			} else if(arg.equals("-d0")){
				_cmdMap.put("关闭明细清单", "true");
			} else if(arg.startsWith("sort=")){
				String sort = arg.substring(5);
				_cmdMap.put("排序列", sort);
			} else {
				// 用户自定义的参数，格式如果不是 name=value形式，则整个存储map中
				int loc = arg.indexOf("=");
				if(loc<0){
					_cmdMap.put(arg, arg);
				} else {
					_cmdMap.put(arg.substring(0, loc), arg.substring(loc+1));
				}
			}
		}
		return _cmdMap;
	}
}
