package fd.ng.core.exception;

import fd.ng.core.exception.internal.RevitalizedCheckedException;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class RevitalizedCheckedExceptionTest extends FdBaseTestCase {
	@Ignore("人工观察异常")
	@Test
	public void observeRawException() {
		try {
			throw new SQLException("Original Exception.");
		} catch (SQLException e) {
			TestCaseLog.println("原始异常：getMessage=" + e.getMessage());
			e.printStackTrace();
			try {
				throw new Exception("New Exception.", e);
			} catch (Exception ex) {
				TestCaseLog.println("包装一层后的异常：getMessage=" + e.getMessage());
				ex.printStackTrace();
			}
		}
	}

	@Test
	public void testGetMessage_0() {
		try {
			throw new RevitalizedCheckedException();
		} catch (RevitalizedCheckedException e) {
			assertThat(e.getMessage(), is(nullValue()));
			assertThat(e.getMineMessage(), is(nullValue()));
			assertThat(e.getCauseMessage(), is(nullValue()));
		}
	}

	@Test
	public void testGetMessage_1() {
		try {
			throw new RevitalizedCheckedException("系统内部错误");
		} catch (RevitalizedCheckedException e) {
			String ex = e.getMessage();
			assertThat(e.getMessage(), is("系统内部错误"));
			assertThat(e.getMineMessage(), is("系统内部错误"));
			assertThat(e.getCauseMessage(), is(nullValue()));
		}
	}

	@Test
	public void testGetMessage_2() {
		try {
			throw new SQLException("数据访问无效。[原信息]");
		} catch (SQLException sqle) {
			try {
				throw new RevitalizedCheckedException(sqle);
			} catch (RevitalizedCheckedException e) {
				String ex = e.getMessage();
				assertThat(e.getMessage(), is("Original Exception : java.sql.SQLException: 数据访问无效。[原信息]"));
				assertThat(e.getMineMessage(), is(nullValue()));
				assertThat(e.getCauseMessage(), is("数据访问无效。[原信息]"));
			}
		}
	}

	@Test
	public void testGetMessage_3() {
		try {
			throw new SQLException("数据访问无效。[原信息]");
		} catch (SQLException sqle) {
			try {
				throw new RevitalizedCheckedException("系统内部错误", sqle);
			} catch (RevitalizedCheckedException e) {
				assertThat(e.getMessage(), is("系统内部错误 ; Original Exception : java.sql.SQLException: 数据访问无效。[原信息]"));
				assertThat(e.getMineMessage(), is("系统内部错误"));
				assertThat(e.getCauseMessage(), is("数据访问无效。[原信息]"));
			}
		}
	}
}
