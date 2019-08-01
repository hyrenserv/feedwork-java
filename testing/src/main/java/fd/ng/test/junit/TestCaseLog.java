package fd.ng.test.junit;

import fd.ng.test.junit.conf.TestinfoConf;

public class TestCaseLog {
	public static void println() {
		if(TestinfoConf.ShowTestCaseLog) System.out.println();
	}
	public static void println(String msg, Object... args) {
		if(TestinfoConf.ShowTestCaseLog) {
			if (args == null || args.length < 1) System.out.println("<TEST> " + msg);
			else {
				System.out.printf("<TEST> " + msg + "%n", args);
			}
		}
	}
}
