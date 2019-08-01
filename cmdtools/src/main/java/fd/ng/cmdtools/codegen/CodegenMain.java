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
				.addOption("codedir", "目录", "本自动生成的项目代码根目录", true)
				.addOption("basepkg", "包名", "项目的基本包名", true)
				.addOption("ftldir",  "目录", "模版文件所在的根目录", false)
				.addOption("-E",                     "可选参数：是否从DB中生成实体", false)
				.parse(args);
	}

	@Override
	public void start() {
		// 创建项目的骨架，生成初始的各种公用java类
		ProjectStructCreator creator = new ProjectStructCreator(
				cmd.option("codedir").value, cmd.option("basepkg").value, cmd.option("ftldir").value);
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
