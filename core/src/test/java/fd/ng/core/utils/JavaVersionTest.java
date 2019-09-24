package fd.ng.core.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JavaVersionTest {
	@Test
	public void testGetJavaVersion() {
		assertThat(JavaVersion.get("1.4"), is(JavaVersion.JAVA_1_4));
		assertThat(JavaVersion.get("1.5"), is(JavaVersion.JAVA_1_5));
		assertThat(JavaVersion.get("1.6"), is(JavaVersion.JAVA_1_6));
		assertThat(JavaVersion.get("1.7"), is(JavaVersion.JAVA_1_7));
		assertThat(JavaVersion.get("1.8"), is(JavaVersion.JAVA_1_8));
		assertThat(JavaVersion.get("9"), is(JavaVersion.JAVA_9));
		assertThat(JavaVersion.get("10"), is(JavaVersion.JAVA_10));
		assertThat(JavaVersion.get("11"), is(JavaVersion.JAVA_11));
		assertThat(JavaVersion.get("12"), is(JavaVersion.JAVA_12));
		assertThat(JavaVersion.get("13"), is(JavaVersion.JAVA_13));

		assertThat(JavaVersion.getJavaVersion("1.8"), is(JavaVersion.get("1.8")));

		JavaVersion javaNone = JavaVersion.get("14");
		JavaVersion javaRcnt = JavaVersion.JAVA_RECENT;
//		assertThat(javaNone, is(javaRcnt));
	}

	@Test
	public void testAtLeast() {
		assertThat(JavaVersion.JAVA_1_4.atLeast(JavaVersion.JAVA_1_5), is(false));
		assertThat(JavaVersion.JAVA_1_5.atLeast(JavaVersion.JAVA_1_4), is(true));
		assertThat(JavaVersion.JAVA_1_8.atLeast(JavaVersion.JAVA_1_7), is(true));
		assertThat(JavaVersion.JAVA_1_8.atLeast(JavaVersion.JAVA_9), is(false));
	}

	@Test
	public void testToString() {
		assertEquals("1.8", JavaVersion.JAVA_1_8.toString());
	}
}