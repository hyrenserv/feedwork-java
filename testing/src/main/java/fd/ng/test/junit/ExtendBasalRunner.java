package fd.ng.test.junit;

import fd.ng.test.junit.rules.anno.Parallel;
import fd.ng.test.junit.rules.anno.Repeat;
import fd.ng.test.junit.rules.anno.Retry;
import fd.ng.test.junit.rules.anno.Timeout;
import fd.ng.test.junit.stmt.*;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * BlockJUnit4ClassRunner是JUnit4的默认Runner实现。有几个重要的方法
 * 1) getChildren()     : 反射扫描获取@Test注解的方法
 * 2) describeChild()   : 对测试方法创建Description并进行缓存
 * 3) runChild()        :
 *                      1、调用describeChild()
 *                      2、判断方法是否包含@Ignore注解，有就触发TestIgnored事件通知
 *                      3、构造Statement回调，通过 methodBlock() 构造并装饰测试方法
 *                      4、调用父类的runLeaf方法用于执行测试方法：调用statement.evaluate()
 *
 * 4) methodBlock()     : 通过反射新建一个测试类的实例，所以每个@Test执行的时候的测试类都是一个新的对象
 *                        因为其内部是通过使用无参的构造方法来创建对象，所以就导致没有测试方法不能有参数
 *                        内部对以下注解做了处理：
 *                        １、@Test(expected=XXX) 如果指定了期望的异常，则包装一层ExpectException，是Statement的子类
 *                        ２、@Test(timeout=10)，对超时时间的指定，包装一层FailOnTimeout
 *                        ３、@Before，包装RunBefores
 *                        ４、@After，包装RunAfters
 *                        ５、@Rule，根据Rule的顺序继续包装
 */
public class ExtendBasalRunner extends BlockJUnit4ClassRunner {
	public ExtendBasalRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected Statement methodBlock(FrameworkMethod method) {
		Object test;
		try {
			test = new ReflectiveCallable() {
				@Override
				protected Object runReflectiveCall() throws Throwable {
					return createTest();
				}
			}.run();
		} catch (Throwable e) {
			return new Fail(e);
		}

		/**
		 * 以下处理逻辑的原理：
		 * 每次根据待处理的规则创建一个 Statement
		 * 记录传入的 Statement 对象
		 * 编写本 Statement 要处理的逻辑，比如先执行 before ，再执行传入的 Statement 的处理逻辑
		 * 因为所有 Statement 的处理逻辑都在其 evaluate 方法中编写，所以可以依次传递下去
		 */
		Statement statement = methodInvoker(method, test); // 原始测试类和方法的 Statement，执行其 evaluate 即为执行测试方法

		// 并行处理的方式
		Parallel annoParallel = method.getAnnotation(Parallel.class);
		int scope = Integer.MAX_VALUE;
		if(annoParallel!=null) scope = annoParallel.scope();
		if(scope!=Integer.MAX_VALUE) {
			if( scope!=Parallel.OnlyTestMethod && scope!=Parallel.WithBeforeAfter )
				throw new ParallelProcessor.IllegalScopeException("scope must be " + Parallel.OnlyTestMethod +" or " + Parallel.WithBeforeAfter);
			scope = annoParallel.scope();
		}

		// 如果有timeout, retry, repeat等注解，则依次修改调用链条
		statement = withTimeout(method, statement);
		statement = withRetry(method, statement);
		statement = withRepeat(method, statement); // repeat 在后，意味着每次repeat中，会retry

		// 如果有异常，则修改调用链条：在try...catch中执行上面的测试方法，然后判断预期异常是否出现。见 ExpectException的evaluate()方法
		statement = possiblyExpectingExceptions(method, test, statement);

		if(scope==Parallel.OnlyTestMethod) statement = withParallel(method, statement);

		// 如果有before，则修改调用链条：先执行完所有的before，再执行测试方法（见 RunBefores evaluate）
		statement = withBefores(method, test, statement);

		// 如果有after，则修改调用链条：执行完测试方法后，再执行after方法
		statement = withAfters(method, test, statement);

		if(scope==Parallel.WithBeforeAfter) statement = withParallel(method, statement);

		// 如果有Rule，则修改调用链条，增加对Rule的处理代码的执行（即Rule中的apply里面的处理代码）
		// 这种流程，也意味着定义的每个规则，都在 before/after 之外进行处理，即：执行顺序为 before -> 测试方法 -> after -> 规则处理逻辑
		statement = withRules(method, test, statement);

		return statement;
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		return new MyInitStatement(method, test);
	}

	protected Statement withTimeout(FrameworkMethod method, Statement next) {
		Test annoTest = method.getAnnotation(Test.class);
		if(annoTest!=null) { // 屏蔽 Junit 自带的timeout处理。因为他是新建线程来执行测试方法，打乱的这个处理逻辑
			if(annoTest.timeout()>0) throw new UnsupportedOperationException("Unsupport timeout in Test annotation!");
		}
		Timeout annotation = method.getAnnotation(Timeout.class);
		if(annotation==null) return next;
		else return new TimeoutProcessor(next, method, annotation.value(), annotation.showFlag());
	}

	protected Statement withRetry(FrameworkMethod method, Statement next) {
		Retry annotation = method.getAnnotation(Retry.class);
		if(annotation==null) return next;
		else return new RetryProcessor(next, method, annotation.value(), annotation.showFlag());
	}

	protected Statement withRepeat(FrameworkMethod method, Statement next) {
		Repeat annotation = method.getAnnotation(Repeat.class);
		if(annotation==null) return next;
		else return new RepeatProcessor(next, method, annotation.value(), annotation.showFlag());
	}

	protected Statement withParallel(FrameworkMethod method, Statement next) {
		Parallel annotation = method.getAnnotation(Parallel.class);
		if(annotation==null) return next;
		else return new ParallelProcessor(next, method, annotation.value(), annotation.timeout(), annotation.scope());
	}

	private Statement withRules(FrameworkMethod method, Object target,
	                            Statement statement) {
		List<TestRule> testRules = getTestRules(target);
		Statement result = statement;
		result = withMethodRules(method, testRules, target, result);
		result = withTestRules(method, testRules, result);

		return result;
	}

	private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules,
	                                  Object target, Statement result) {
		for (org.junit.rules.MethodRule each : getMethodRules(target)) {
			if (!testRules.contains(each)) {
				result = each.apply(result, method, target);
			}
		}
		return result;
	}

	private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules,
	                                Statement statement) {
		return testRules.isEmpty() ? statement :
				new RunRules(statement, testRules, describeChild(method));
	}

	private List<org.junit.rules.MethodRule> getMethodRules(Object target) {
		return rules(target);
	}
}
