package fd.ng.core.conf;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.StringUtil;

import java.io.File;

public class ConfFileLoader {

	/**
	 * 获取 conf 文件的位置
	 * 根据 filename 先取系统参数，如果没有，再从 classpath 下取文件
	 * @param filename 配置文件的名字，不需要后缀（系统定死了后缀必须是 conf）
	 * @return File 或者 String。File意味着从硬盘绝对路径上获取配置文件，String意味着从classpath中获取文件
	 */
	public static Object getConfFile(String filename) {
		if(StringUtil.isEmpty(filename)) throw new FrameworkRuntimeException("conf filename must not null");
		String filepath = System.getProperty("fdconf."+filename);
		if (filepath == null) {
			return "fdconfig/"+filename+".conf";
		} else { // 通过系统参数指定了配置文件。必须是硬盘绝对路径
			File confFile = new File(filepath);
			if(!confFile.exists()) throw new FrameworkRuntimeException("conf file[ "+filepath+" ] not exist!");
			else if(!confFile.isFile()) throw new FrameworkRuntimeException("conf file[ "+filepath+" ] must be real file!");
			return confFile;
		}
	}
}
