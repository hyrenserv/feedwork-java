package fd.ng.cmdtools.ioeva;

import fd.ng.cmdtools.CmdMain;
import fd.ng.core.cmd.ArgsParser;

public class CmdMainImpl implements CmdMain {
	private ArgsParser cmd;
	public CmdMainImpl(String[] args) {
		cmd = new ArgsParser()
				.addOption("file",   "文件名",   "读写测试的文件名", true)
				.addOption("type",   "r|w|rw",   "读写测试类型", true)
				.addOption("engine", "ok|jdk",   "用什么引擎写", true)
				.addOption("wopt",   "raw|sbuf", "写的方式：raw为直接写，sbuf为追加到StringBuilder再写。默认为raw", false)
				.addOption("rows",   "数字",     "写文件的总行数", false)
				.addOption("-fw",                       "仅mode=jdk有效，是每次写都flush-也就是当为写的时候，wopt的值就是一次flush+write的量", false)
				.parse(args);
	}

	@Override
	public void start() {
		AccessEvaluate eva = new AccessEvaluate(cmd.option("file").value);
		if(cmd.option("type").is("w")) {
			if(cmd.option("engine").is("ok"))
				eva.writeByOkio(
						Integer.parseInt(cmd.option("rows").value),
						cmd.option("wopt").value
				);
			else if(cmd.option("engine").is("jdk"))
				eva.writeByJdkio(
						Integer.parseInt(cmd.option("rows").value),
						Integer.parseInt(cmd.option("wopt").value),
						cmd.option("-fw").exist()
				);
			else
				throw new RuntimeException("Wrong arguments : engine="+ cmd.option("engine").value);
		} else if(cmd.option("type").is("rw")) {
			if(cmd.option("engine").is("ok"))
				eva.rpwByOkio();
			else if(cmd.option("engine").is("jdk"))
				eva.rpwByJdkio();
			else
				throw new RuntimeException("Wrong arguments : engine="+ cmd.option("engine").value);
		}
		else
			throw new RuntimeException("Wrong arguments : type="+ cmd.option("type").value);
	}

	@Override
	public void usage() {
		cmd.usage();
	}
}
