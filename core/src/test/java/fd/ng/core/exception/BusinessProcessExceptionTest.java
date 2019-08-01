package fd.ng.core.exception;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RuntimeOnlyMessageException;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class BusinessProcessExceptionTest extends FdBaseTestCase {
	@Ignore("人工观察异常")
	@Test
	public void observeRawException() {
		try {
			throw new SQLException("第一次抛出的SQL异常的message");
		} catch (SQLException e) {
			TestCaseLog.println("原始异常：getMessage=" + e.getMessage());
//			e.printStackTrace();
			try {
				throw new IllegalStateException("New Exception.", e);
			} catch (Exception ex) {
				TestCaseLog.println("包装一层后的异常：getMessage=" + e.getMessage());
//				ex.printStackTrace();
//				throw new RuntimeOnlyMessageException("xxxxxxxxx", ex);
				throw new BusinessProcessException("bizException");
			}
		}
	}

	// 测试构建提示消息的异常
	@Test
	public void test_0() {
		try {
			throw new BusinessProcessException("only msg");
		} catch (BusinessProcessException e) {
			String els = e.getLocalizedMessage();
			assertThat(e.getMessage(), is("only msg"));
			assertThat(e.getResourceKeyName(), is(nullValue()));
			assertThat(e.getResourceArgs(), is(nullValue()));
		}
	}

	// 测试构建无参资源代码名
	@Test
	public void test_1() {
		try {
			// 这个定义在 biz_errors.properties 文件中
			throw new BusinessProcessException("hmfms.a0101.userlist", null);
		} catch (BusinessProcessException e) {
			assertThat(e.getMessage(), is("User list!"));
			assertThat(e.getResourceKeyName(), is("hmfms.a0101.userlist"));
			assertThat(e.getResourceArgs(), is(new Object[]{}));
		}
	}

	// 测试构建有参资源代码名
	@Test
	public void test_2() {
		Object[] args = new Object[]{
				"FD飞", 123
		};
		try {
			// 这个定义在 biz_errors_zh_CN.properties 文件中
			throw new BusinessProcessException("hmfms.a0101.userinfo", args);
		} catch (BusinessProcessException e) {
			String els = e.getLocalizedMessage();
			assertThat(e.getMessage(), is("type of 用户(FD飞) is 123. zh_CN"));
			assertThat(e.getResourceKeyName(), is("hmfms.a0101.userinfo"));
			assertThat(e.getResourceArgs(), is(args));
		}
	}
}
