package fd.ng.cmdtools.ioeva;

import fd.ng.cmdtools.CmdMain;
import fd.ng.core.cmd.ArgsParser;

public class CmdMainImpl implements CmdMain {
	private ArgsParser cmd;
	public CmdMainImpl(String[] args) {
		cmd = new ArgsParser()
				.defOptionPair("file",   true,   "读写测试的文件名")
				.defOptionPair("type",   true,   "读写测试类型")
				.defOptionPair("engine", true,   "用什么引擎写")
				.defOptionPair("wopt",   false,  "写的方式：raw为直接写，sbuf为追加到StringBuilder再写。默认为raw")
				.defOptionPair("rows",   false,  "写文件的总行数")
				.defOptionSwitch("-fw",  false,  "仅mode=jdk有效，是每次写都flush-也就是当为写的时候，wopt的值就是一次flush+write的量")
				.parse(args);
	}

	@Override
	public void start() {
		AccessEvaluate eva = new AccessEvaluate(cmd.opt("file").value);
		if(cmd.opt("type").is("w")) {
			if(cmd.opt("engine").is("ok"))
				eva.writeByOkio(
						Integer.parseInt(cmd.opt("rows").value),
						cmd.opt("wopt").value
				);
			else if(cmd.opt("engine").is("jdk"))
				eva.writeByJdkio(
						Integer.parseInt(cmd.opt("rows").value),
						Integer.parseInt(cmd.opt("wopt").value),
						cmd.opt("-fw").exist()
				);
			else
				throw new RuntimeException("Wrong arguments : engine="+ cmd.opt("engine").value);
		} else if(cmd.opt("type").is("rw")) {
			if(cmd.opt("engine").is("ok"))
				eva.rpwByOkio();
			else if(cmd.opt("engine").is("jdk"))
				eva.rpwByJdkio();
			else
				throw new RuntimeException("Wrong arguments : engine="+ cmd.opt("engine").value);
		}
		else
			throw new RuntimeException("Wrong arguments : type="+ cmd.opt("type").value);
	}

	@Override
	public void usage() {
		cmd.usage();
	}
}
