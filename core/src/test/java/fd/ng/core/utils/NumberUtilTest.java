package fd.ng.core.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NumberUtilTest {

	@Test
	public void isNumberic() {
		assertThat(NumberUtil.isNumberic(null), is(false));
		assertThat(NumberUtil.isNumberic(""), is(false));
		assertThat(NumberUtil.isNumberic(" "), is(false));
		assertThat(NumberUtil.isNumberic("abc"), is(false));
		assertThat(NumberUtil.isNumberic("123a"), is(false));
		assertThat(NumberUtil.isNumberic("b35"), is(false));
		assertThat(NumberUtil.isNumberic("1-2"), is(false));
		assertThat(NumberUtil.isNumberic(".2"), is(false));
		assertThat(NumberUtil.isNumberic("-.2"), is(false));
		assertThat(NumberUtil.isNumberic("23."), is(false));
		assertThat(NumberUtil.isNumberic("23.34.34"), is(false));
		assertThat(NumberUtil.isNumberic("-0"), is(false));
		assertThat(NumberUtil.isNumberic("\u0967\u0968\u0969"), is(false));

		// 验证正确的数
		assertThat(NumberUtil.isNumberic("-23.3"), is(true));
		assertThat(NumberUtil.isNumberic("0.2"), is(true));
		assertThat(NumberUtil.isNumberic("-0.2"), is(true));
		assertThat(NumberUtil.isNumberic("2.2"), is(true));
		assertThat(NumberUtil.isNumberic("2.0"), is(true));
		assertThat(NumberUtil.isNumberic("02.00"), is(true));

		assertThat(NumberUtil.isNumberic("0"), is(true));
		assertThat(NumberUtil.isNumberic("-2"), is(true));
		assertThat(NumberUtil.isNumberic("2"), is(true));
		assertThat(NumberUtil.isNumberic("28987349584987598476565467546"), is(true));

		// 正数验证
		assertThat(NumberUtil.isPositivNumberic("-2"), is(false));
		assertThat(NumberUtil.isPositivNumberic("-245"), is(false));
		assertThat(NumberUtil.isPositivNumberic("-234.46"), is(false));
		assertThat(NumberUtil.isPositivNumberic("340.8787"), is(true));
		assertThat(NumberUtil.isPositivNumberic("20"), is(true));
	}

	@Test
	public void isFloat() {
		assertThat(NumberUtil.isFloat(null), is(false));
		assertThat(NumberUtil.isFloat(""), is(false));
		assertThat(NumberUtil.isFloat(" "), is(false));
		assertThat(NumberUtil.isFloat("abc"), is(false));
		assertThat(NumberUtil.isFloat("123a"), is(false));
		assertThat(NumberUtil.isFloat("b35"), is(false));
		assertThat(NumberUtil.isFloat("1-2"), is(false));
		assertThat(NumberUtil.isFloat(".2"), is(false));
		assertThat(NumberUtil.isFloat("-.2"), is(false));
		assertThat(NumberUtil.isFloat("23."), is(false));
		assertThat(NumberUtil.isFloat("23.34.34"), is(false));
		assertThat(NumberUtil.isFloat("-0"), is(false));
		assertThat(NumberUtil.isFloat("\u0967\u0968\u0969"), is(false));

		assertThat(NumberUtil.isFloat("0"), is(false));
		assertThat(NumberUtil.isFloat("-2"), is(false));
		assertThat(NumberUtil.isFloat("-245"), is(false));
		assertThat(NumberUtil.isFloat("2"), is(false));
		assertThat(NumberUtil.isFloat("28987349584987598476565467546"), is(false));

		// 验证正确的数
		assertThat(NumberUtil.isFloat("0.0"), is(true));
		assertThat(NumberUtil.isFloat("-0.0"), is(true));
		assertThat(NumberUtil.isFloat("-23.3"), is(true));
		assertThat(NumberUtil.isFloat("0.2"), is(true));
		assertThat(NumberUtil.isFloat("-0.2"), is(true));
		assertThat(NumberUtil.isFloat("2.2"), is(true));
		assertThat(NumberUtil.isFloat("2.0"), is(true));
		assertThat(NumberUtil.isFloat("02.00"), is(true));

		// 正数验证
		assertThat(NumberUtil.isPositiveFloat("0.0"), is(true));
		assertThat(NumberUtil.isPositiveFloat("-2.34"), is(false));
		assertThat(NumberUtil.isPositiveFloat("-234.46"), is(false));
		assertThat(NumberUtil.isPositiveFloat("340.8787"), is(true));
		assertThat(NumberUtil.isPositiveFloat("20.0"), is(true));
	}

	@Test
	public void isInteger() {
		assertThat(NumberUtil.isInteger(null), is(false));
		assertThat(NumberUtil.isInteger(""), is(false));
		assertThat(NumberUtil.isInteger(" "), is(false));
		assertThat(NumberUtil.isInteger("abc"), is(false));
		assertThat(NumberUtil.isInteger("123a"), is(false));
		assertThat(NumberUtil.isInteger("b35"), is(false));
		assertThat(NumberUtil.isInteger("1-2"), is(false));
		assertThat(NumberUtil.isInteger(".2"), is(false));
		assertThat(NumberUtil.isInteger("-.2"), is(false));
		assertThat(NumberUtil.isInteger("23."), is(false));
		assertThat(NumberUtil.isInteger("23.34.34"), is(false));
		assertThat(NumberUtil.isInteger("-0"), is(false));
		assertThat(NumberUtil.isInteger("\u0967\u0968\u0969"), is(false));

		assertThat(NumberUtil.isInteger("-23.3"), is(false));
		assertThat(NumberUtil.isInteger("0.2"), is(false));
		assertThat(NumberUtil.isInteger("-0.2"), is(false));
		assertThat(NumberUtil.isInteger("2.2"), is(false));
		assertThat(NumberUtil.isInteger("28.0"), is(false));
		assertThat(NumberUtil.isInteger("02.00"), is(false));

		// 验证正确的数
		assertThat(NumberUtil.isInteger("0"), is(true));
		assertThat(NumberUtil.isInteger("-2"), is(true));
		assertThat(NumberUtil.isInteger("-245"), is(true));
		assertThat(NumberUtil.isInteger("2"), is(true));
		assertThat(NumberUtil.isInteger("28987349584987598476565467546"), is(true));

		// 正数验证
		assertThat(NumberUtil.isPositiveInteger("0"), is(true));
		assertThat(NumberUtil.isPositiveInteger("-2"), is(false));
		assertThat(NumberUtil.isPositiveInteger("-234"), is(false));
		assertThat(NumberUtil.isPositiveInteger("340"), is(true));
		assertThat(NumberUtil.isPositiveInteger("2"), is(true));
	}

	@Test
	public void perf() {
		NumberUtil.isNumberic("357");
		StopWatch watch = new StopWatch();
		watch.start();
		for(int i=0; i<1_000_000; i++) {
			NumberUtil.isNumberic("35774"+i);
			NumberUtil.isNumberic("-4546"+i);
		}
		watch.stop();
		long msTimes = watch.getTotalTime();

//		System.out.println("times : " + msTimes);
		assertThat("酷睿 i5 六代笔记本 平均在155毫秒左右", msTimes<300, is(true));
	}
}