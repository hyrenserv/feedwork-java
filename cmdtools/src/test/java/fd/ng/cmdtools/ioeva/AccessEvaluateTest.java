package fd.ng.cmdtools.ioeva;

import org.junit.Test;

public class AccessEvaluateTest {

	@Test
	public void writeJdkio() {
		int rows = 1000;
		CmdMainImpl main = new CmdMainImpl(new String[]{
				"type=w", "engine=jdk", "wopt=" + 500, // 一次flush的数量
				"rows="+rows,
				"-fw",
				"file=/tmp/io-jdk.csv"
		});
		main.start();

	}

	@Test
	public void writeOkio() {
		int rows = 100;
		CmdMainImpl mainOK = new CmdMainImpl(new String[]{
				"type=w", "engine=ok", "wopt=raw",
				"rows="+rows,
				"file=/tmp/io-ok.csv"
		});
		mainOK.start();
	}

	@Test
	public void readAndWriteJdkio() {
		CmdMainImpl mainOK = new CmdMainImpl(new String[]{
				"type=rw", "engine=jdk",
				"file=/tmp/io.csv"
		});
		mainOK.start();
	}
	@Test
	public void readAndWriteOkio() {
		CmdMainImpl mainOK = new CmdMainImpl(new String[]{
				"type=rw", "engine=ok",
				"file=/tmp/io.csv"
		});
		mainOK.start();
	}
}