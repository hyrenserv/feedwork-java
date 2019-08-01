package fd.ng.test.junit.rules;

import fd.ng.core.conf.AppinfoConf;
import org.junit.runner.Description;

public abstract class AbstractRule {
	public String getAppliedMethodName(final Description description) {
		return description.getClassName().replace(AppinfoConf.AppBasePackage, "..")
				+ "."+description.getMethodName()+"()";
	}
}
