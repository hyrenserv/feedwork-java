package fd.ng.core.conf;

import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import fd.ng.core.yaml.YamlReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppinfoConf {
	public static final String AppBasePackage;
	public static final boolean LoggedExceptionRaw;   // 是否把 RawlayerRuntimeException 异常堆栈自动写入日志
	public static final boolean LoggedExceptionFrame; // 是否把 FrameworkRuntimeException 异常堆栈自动写入日志
	static {
		YamlMap rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("appinfo")).asMap();

		AppBasePackage = rootConfig.getString("basePackage", "fdapp");
		LoggedExceptionRaw = rootConfig.getBool("loggedException.Raw", true);
		LoggedExceptionFrame = rootConfig.getBool("loggedException.Frame", true);
	}
}
