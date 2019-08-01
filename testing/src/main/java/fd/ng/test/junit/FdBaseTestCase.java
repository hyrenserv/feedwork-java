package fd.ng.test.junit;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

public class FdBaseTestCase {
	@Rule public RunTimeWatcher G_RunTimeWatcher = new RunTimeWatcher();
	/** 子类中，任意位置直接使用 G_TestMethod.getMethodName() ，可以得到当前的测试方法名 */
	@Rule public TestName G_TestMethod = new TestName();
//	@Rule public RetryRule G_RetryRule = new RetryRule();
//	@Rule public RepeatRule G_RepeatRule = new RepeatRule();
//	@Rule public ParallelRule G_ParallelRule = new ParallelRule();

	// 在测试方法中，使用： expectedEx.expect(XXXException.class); 判断预期的异常是否出现了
	@Rule public ExpectedException G_ExpectedEx = ExpectedException.none();

	protected String getCurrentMethodName(){
		StackTraceElement[] stackTraceArr = Thread.currentThread().getStackTrace();
		int i=0;
		for(; i<stackTraceArr.length; i++){
			StackTraceElement curStackTrace = stackTraceArr[i];
//			System.out.println("%2d : className = %s, fileName = %s, lineNumber = %s, methodName = %s\n"
//					, i
//					, curStackTrace.getClassName()
//					, curStackTrace.getFileName()
//					, curStackTrace.getLineNumber()
//					, curStackTrace.getMethodName()
//			);
			String className = curStackTrace.getClassName();
			String methodName = curStackTrace.getMethodName();
			if(className.equals(FdBaseTestCase.class.getName())){
				if("startTest".equals(methodName)||"endTest".equals(methodName))
					break;
			}
		}
		StackTraceElement e = stackTraceArr[i+1];
		String className = e.getClassName();
		className = className.substring(className.lastIndexOf(".")+1);
		return className + "." + e.getMethodName();
	}
}
