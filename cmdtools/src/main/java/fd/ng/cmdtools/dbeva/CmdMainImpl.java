package fd.ng.cmdtools.dbeva;

import fd.ng.cmdtools.CmdMain;
import fd.ng.core.cmd.ArgsParser;

public class CmdMainImpl implements CmdMain {
	private ArgsParser cmd;
	public CmdMainImpl(String[] args) {
		cmd = new ArgsParser()
				.addOption("tbl",   "DB表名",  "读写测试的文件名", true)
				.addOption("type",  "r|w|d|c", "读、写、删除表、创建表", true)
				.addOption("total", "数值",    "写入总量，或者，多线程分页读取时的总量", false)
				.addOption("pagen", "数值",    "多线程分页读取的每页大小，如果小于1，则单线程一次读取", false)
				.addOption("ban",   "数值",    "写入数据时，每个批次的数量。必须小于等于'total'", false)
				.parse(args);
	}

	@Override
	public void start() {
		AccessEvaluate eva = new AccessEvaluate(cmd.option("tbl").value);
		if(cmd.option("type").isIgnoreCase("r")) {
			if(cmd.option("total").isNoValue) {
				eva.readOneThread();
			} else {
				int total = Integer.valueOf(cmd.option("total").value);
				if(cmd.option("pagen").isNoValue)
					throw new RuntimeException("when type=r and total not null, [pagen] must not null!");
				int pagen = Integer.valueOf(cmd.option("pagen").value);
				if (pagen < 1) eva.readOneThread();
				else eva.read(total, pagen);
			}
		} else if(cmd.option("type").isIgnoreCase("w")) {
			if(cmd.option("total").isNoValue||cmd.option("ban").isNoValue)
				throw new RuntimeException("when type=w, [total] or [ban] must not null!");
			int total = Integer.valueOf(cmd.option("total").value);
			int batn = Integer.valueOf(cmd.option("ban").value);
			if(batn>total) batn = total;
			eva.write(total, batn);
		} else if(cmd.option("type").isIgnoreCase("d")) {
			eva.clear();
		} else if(cmd.option("type").isIgnoreCase("c")) {
			eva.createTestTableIfNotExists();
		}
		else throw new RuntimeException("读写测试参数不合法");
	}

	@Override
	public void usage() {
		cmd.usage();
	}
}