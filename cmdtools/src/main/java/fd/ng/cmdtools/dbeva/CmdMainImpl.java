package fd.ng.cmdtools.dbeva;

import fd.ng.cmdtools.CmdMain;
import fd.ng.core.cmd.ArgsParser;

public class CmdMainImpl implements CmdMain {
	private ArgsParser cmd;
	public CmdMainImpl(String[] args) {
		cmd = new ArgsParser()
				.defOptionPair("tbl",   true,  "DB表名")
				.defOptionPair("type",  true, "r|w|rw 读、写、删除表、创建表")
				.defOptionPair("total", false,    "写入总量，或者，多线程分页读取时的总量")
				.defOptionPair("pagen", false,    "多线程分页读取的每页大小，如果小于1，则单线程一次读取")
				.defOptionPair("ban",   false,    "写入数据时，每个批次的数量。必须小于等于'total'")
				.parse(args);
	}

	@Override
	public void start() {
		AccessEvaluate eva = new AccessEvaluate(cmd.opt("tbl").value);
		if(cmd.opt("type").isIgnoreCase("r")) {
			if(cmd.opt("total").notExist()) {
				eva.readOneThread();
			} else {
				int total = Integer.valueOf(cmd.opt("total").value);
				if(cmd.opt("pagen").notExist())
					throw new RuntimeException("when type=r and total not null, [pagen] must not null!");
				int pagen = Integer.valueOf(cmd.opt("pagen").value);
				if (pagen < 1) eva.readOneThread();
				else eva.read(total, pagen);
			}
		} else if(cmd.opt("type").isIgnoreCase("w")) {
			if(cmd.opt("total").notExist()||cmd.opt("ban").notExist())
				throw new RuntimeException("when type=w, [total] or [ban] must not null!");
			int total = Integer.valueOf(cmd.opt("total").value);
			int batn = Integer.valueOf(cmd.opt("ban").value);
			if(batn>total) batn = total;
			eva.write(total, batn);
		} else if(cmd.opt("type").isIgnoreCase("d")) {
			eva.clear();
		} else if(cmd.opt("type").isIgnoreCase("c")) {
			eva.createTestTableIfNotExists();
		}
		else throw new RuntimeException("读写测试参数不合法");
	}

	@Override
	public void usage() {
		cmd.usage();
	}
}