package ${basePackage}.${subPackage};

import fd.ng.test.junit.ParallelRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.stream.Collectors;

public class ParallelRunnerTest {
	public static void main(String[] args) {
		Class[] cls = { TestCase1.class, TestCase2.class };
		Result rt;

		// 多个类并发执行
//		rt = JUnitCore.runClasses(ParallelRunner.classes(), cls);

		// 多个类串行，类中的方法并行
//		rt = JUnitCore.runClasses(ParallelRunner.methods(), cls);

		// 类和方法都并行
		rt = JUnitCore.runClasses(new ParallelRunner(true, true), cls);

		System.out.println("wasSuccessful=" + rt.wasSuccessful() + ", getIgnoreCount=" + rt.getIgnoreCount());
		System.out.println("getRunCount=" + rt.getRunCount() + ", getRunTime=" + rt.getRunTime());
		System.out.println("getFailureCount=" + rt.getFailureCount() + ", getRunTime=" + rt.getFailures().stream().map(Failure::toString).collect(Collectors.joining(" | ")));
	}
}
