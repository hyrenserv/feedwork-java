package fd.ng.core.utils;

import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.RunTimeWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

@Ignore
public class LoggerTest extends FdBaseTestCase {
	private static final Logger logger = LogManager.getLogger(LoggerTest.class.getName());
	@Rule public RunTimeWatcher runTimeWatcher = new RunTimeWatcher();
	@Rule public TestName testName = new TestName();

	private int count = 100000;


	@Test
	public void perfNo_isDebugEnabled(){
		for(int i=0; i<count; i++){
			logger.debug("test [non if] one argument. {}", i);
		}
	}

	@Test
	public void perfHas_isDebugEnabled(){
		for(int i=0; i<count; i++){
			if(logger.isDebugEnabled())
				logger.debug("test [has if] one argument. {}", i);
		}
	}
}
