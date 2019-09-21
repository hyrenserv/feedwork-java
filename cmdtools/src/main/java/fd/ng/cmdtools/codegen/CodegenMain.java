package fd.ng.cmdtools.codegen;

import fd.ng.cmdtools.CmdMain;
import fd.ng.cmdtools.codegen.creators.ProjectStructCreator;
import fd.ng.core.cmd.ArgsParser;
import fd.ng.db.conf.DbinfosConf;

import java.util.HashMap;
import java.util.Map;

public class CodegenMain implements CmdMain {
	private ArgsParser cmd;
	public CodegenMain(String[] args) {
		cmd = new ArgsParser()
				.defOptionPair("codedir", true, "本自动生成的项目代码根目录")
				.defOptionPair("basepkg", true, "项目的基本包名")
				.defOptionPair("ftldir",  false, "模版文件所在的根目录")
				.defOptionSwitch("-E",    false, "可选参数：是否从DB中生成实体")
				.parse(args);
	}

	@Override
	public void start() {
		// 创建项目的骨架，生成初始的各种公用java类
		ProjectStructCreator creator = new ProjectStructCreator(
				cmd.opt("codedir").value, cmd.opt("basepkg").value, cmd.opt("ftldir").value);
		creator.clearAndCreateProjectDirs();
		creator.genGeneralJavaCodeFiles();
		creator.genGeneralJavaTestCodeFiles();
		creator.genResourceFiles();
		// 创建实体类
		if(cmd.exist("-E"))
			creator.genEntityCodeFiles(DbinfosConf.DEFAULT_DBNAME);
		creator.genGradleFiles();

		creator.infoOutln();
		creator.infoOutln("工程初始代码生成完成");
		creator.infoOutln("如果需要使用自定义的模版文件，可构造List存入各个目录包名，并调用相应的'gen'开头的生成函数");
	}

	@Override
	public void usage() {
		cmd.usage();
	}
}
