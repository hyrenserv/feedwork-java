package fd.ng.test.junit;

import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import fd.ng.core.yaml.YamlReader;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.List;

/**
 * 用法：
 * 新增一个test类，并添加以下类注解
 * <code>
 * @RunWith(RepeatSuite.class)
 * @Suite.SuiteClasses({
 *                FuncStressTestActionTest.class // 被管理的实际测试用例
 * })
 * </code>
 * 在 testinfo.conf 中，把重复测试次数赋值给 repeatTestSuiteCount 变量
 *
 * 不推荐使用被类。在测试方法上使用 Repeat 注解即可（参考 ExtRunnerRepeatTest 里的测试用例）
 */
@Deprecated
public class RepeatSuite extends Suite {
	private static final int RepeatCount;
	static {
		YamlReader reader = YamlFactory.load("fdconfig/testinfo.conf");
		YamlMap rootConfig = reader.asMap();

		RepeatCount = rootConfig.getInt("repeatTestSuiteCount", 10);
	}
	public RepeatSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(klass, builder);
	}

	public RepeatSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
		super(builder, classes);
	}

	protected RepeatSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
		super(klass, suiteClasses);
	}

	protected RepeatSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
		super(builder, klass, suiteClasses);
	}

	protected RepeatSuite(Class<?> klass, List<Runner> runners) throws InitializationError {
		super(klass, runners);
	}

	@Override
	protected void runChild(Runner runner, RunNotifier notifier) {
		TestCaseLog.println("TestSuit [ " + getTestClass().getName() + " ] run count : " + RepeatCount);
		for(int i = 0; i<RepeatCount; i++) {
			runner.run(notifier);
		}
	}
}
