package ${basePackage}.${subPackage};

import fd.ng.test.junit.FdBaseTestCase;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("使用本类做测试时，需要注释掉 BusinessException 里面的记录日志的代码")
public class ${className} extends FdBaseTestCase {
	// 测试构建提示消息的异常
	@Test
	public void test_0() {
		try {
			throw new BusinessException("only msg");
		} catch (BusinessException e) {
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
			throw new BusinessException("hmfms.a0101.userlist", null);
		} catch (BusinessException e) {
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
			throw new BusinessException("hmfms.a0101.userinfo", args);
		} catch (BusinessException e) {
			String els = e.getLocalizedMessage();
			assertThat(e.getMessage(), is("type of 用户(FD飞) is 123. zh_CN"));
			assertThat(e.getResourceKeyName(), is("hmfms.a0101.userinfo"));
			assertThat(e.getResourceArgs(), is(args));
		}
	}
}
