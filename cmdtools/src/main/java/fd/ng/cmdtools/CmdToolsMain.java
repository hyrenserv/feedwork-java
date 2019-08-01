package fd.ng.cmdtools;

import fd.ng.cmdtools.codegen.CodegenMain;
import fd.ng.cmdtools.dbeva.CmdMainImpl;
import fd.ng.cmdtools.loadrunner.LoadRunnerMain;

import java.util.Arrays;

public class CmdToolsMain {
	public static void main(String[] args) {
		String usage = "\njava -jar jarfilename loadrunner/codegen/dbeva/ioeva [OPTIONS]\n";
		if(args.length<1) {
			System.out.println(usage);
			System.exit(-1);
		}
		String toolsName = args[0];
		String[] toolsArgs = Arrays.copyOfRange(args, 1, args.length);
		if("loadrunner".equalsIgnoreCase(toolsName)) {
			LoadRunnerMain loadRunnerMain = new LoadRunnerMain(toolsArgs);
			loadRunnerMain.start();
		} else {
			CmdMain main = null;
			if("codegen".equalsIgnoreCase(toolsName)) {
				main = new CodegenMain(toolsArgs);
			} else if ("dbeva".equalsIgnoreCase(toolsName)) {
				main = new fd.ng.cmdtools.dbeva.CmdMainImpl(toolsArgs);
			} else if ("ioeva".equalsIgnoreCase(toolsName)) {
				main = new fd.ng.cmdtools.ioeva.CmdMainImpl(toolsArgs);
			} else {
				System.out.println(usage);
				System.exit(-1);
			}
			if(toolsArgs.length==1&&("-h".equals(toolsArgs[0])||"?".equals(toolsArgs[0])||"/h".equals(toolsArgs[0])||"help".equals(toolsArgs[0])))
				main.usage();
			else
				main.start();
		}
	}
}
