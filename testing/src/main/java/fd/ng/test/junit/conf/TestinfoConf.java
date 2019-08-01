package fd.ng.test.junit.conf;

import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestinfoConf {
	private static final Logger logger = LogManager.getLogger(TestinfoConf.class.getName());

	public static final boolean ShowTestCaseLog;
	public static final int ParallelRunTimeout; // For ParallelRunner
	public static final int RepeatCount; // For RepeatSuite
	static {
		YamlMap rootConfig;
		try {
			rootConfig = YamlFactory.load(ConfFileLoader.getConfFile("testinfo")).asMap();
		} catch (YamlFactory.YamlFileNotFoundException e) {
			rootConfig = null;
		}
		if(rootConfig==null) {
			ShowTestCaseLog = true;
			ParallelRunTimeout = 30000;
			RepeatCount = 10;
		}
		else {
			ShowTestCaseLog = rootConfig.getBool("showTestCaseLog", true);
			ParallelRunTimeout = rootConfig.getInt("parallel.run.timeout", 30000);
			RepeatCount = rootConfig.getInt("repeatTestSuiteCount", 10);
		}
	}
}
