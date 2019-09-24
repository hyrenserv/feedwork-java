package fd.ng.core.cmd;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArgsParserTest {

	@Test
	public void parse() {
		// 模拟命令行输入的参数。等价命令行语句为：java -jar a.jar file=/tmp/test.log type=r
		String file = "/tmp/test.log";
		String type = "r";
		String[] args = new String[]{
				"file=" + file,
				"rw.type=" + type
		};
		ArgsParser CMD_ARGS = new ArgsParser()
				.defOptionPair("file", true, "读写测试的文件名")
				.defOptionPair("rw.type", true, "r|w|rw 读写测试类型")
				.defOptionSwitch("-fw", false, "是否自动执行 flush")
				.parse(args);
		assertThat(CMD_ARGS.opt("file").value, is(file));
		assertThat(CMD_ARGS.opt("rw.type").value, is(type));
		assertThat(CMD_ARGS.opt("rw.type").exist(), is(true));
		assertThat(CMD_ARGS.opt("rw.type").isIgnoreCase(type.toUpperCase()), is(true));
		assertThat(CMD_ARGS.opt("rw.Type").value, nullValue());
		assertThat(CMD_ARGS.opt("-fw").exist(), is(false));

		// 测试无值的开关参数的正确性
		args = new String[]{
				"file=" + file, "rw.type=" + type,
				"-fw"
		};
		CMD_ARGS = new ArgsParser()
				.defOptionPair("file", true, "读写测试的文件名")
				.defOptionPair("rw.type", true, "r|w|rw 读写测试类型")
				.defOptionSwitch("-fw", true,  "是否自动执行 flush")
				.parse(args);
		assertThat(CMD_ARGS.opt("file").value, is(file));
		assertThat(CMD_ARGS.opt("rw.type").value, is(type));
		assertThat(CMD_ARGS.opt("-fw").exist(), is(true));
	}

	@Ignore("测试 usage ，需要手工执行并观察输出")
	@Test
	public void usage() {
		ArgsParser CMD_ARGS = new ArgsParser()
				.defOptionPair("file",    true, "读写测试的文件名")
				.defOptionPair("rw.type", true, "r|w|rw 读写测试类型")
				.defOptionSwitch("-fw",     false,"是否自动执行flush")
				.parse(new String[]{});
	}

	@Ignore("测试 usage ，需要手工执行并观察输出")
	@Test
	public void usage_help() {
		ArgsParser CMD_ARGS = new ArgsParser()
				.defOptionPair("file", true, "读写测试的文件名")
				.parse(new String[]{"-Help"});
	}
}