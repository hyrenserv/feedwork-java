package fd.ng.core.utils;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Locale;

import static org.junit.Assert.*;

public class SystemUtilTest {
	@Test
	public void testConstructor() {
		final Constructor<?>[] cons = SystemUtil.class.getDeclaredConstructors();
		assertEquals(1, cons.length);
		assertTrue(Modifier.isPublic(cons[0].getModifiers()));
		assertTrue(Modifier.isPublic(SystemUtil.class.getModifiers()));
		assertFalse(Modifier.isFinal(SystemUtil.class.getModifiers()));
	}

	@Test
	public void testGetEnvironmentVariableAbsent() {
		final String name = "THIS_ENV_VAR_SHOULD_NOT_EXIST_FOR_THIS_TEST_TO_PASS";
		final String expected = System.getenv(name);
		assertNull(expected);
		final String value = SystemUtil.getEnvironmentVariable(name, "DEFAULT");
		assertEquals("DEFAULT", value);
	}

	@Test
	public void testGetEnvironmentVariablePresent() {
		final String name = "PATH";
		final String expected = System.getenv(name);
		final String value = SystemUtil.getEnvironmentVariable(name, null);
		assertEquals(expected, value);
	}

	@Test
	public void testGetHostName() {
		final String hostName = SystemUtil.getHostName();
		final String expected = SystemUtil.IS_OS_WINDOWS ? System.getenv("COMPUTERNAME") : System.getenv("HOSTNAME");
		assertEquals(expected, hostName);
	}

	/**
	 * Assumes no security manager exists.
	 */
	@Test
	public void testGetJavaHome() {
		final File dir = SystemUtil.getJavaHome();
		assertNotNull(dir);
		assertTrue(dir.exists());
	}

	/**
	 * Assumes no security manager exists.
	 */
	@Test
	public void testGetJavaIoTmpDir() {
		final File dir = SystemUtil.getJavaIoTmpDir();
		assertNotNull(dir);
		assertTrue(dir.exists());
	}

	/**
	 * Assumes no security manager exists.
	 */
	@Test
	public void testGetUserDir() {
		final File dir = SystemUtil.getUserDir();
		assertNotNull(dir);
		assertTrue(dir.exists());
	}

	/**
	 * Assumes no security manager exists.
	 */
	@Test
	public void testGetUserHome() {
		final File dir = SystemUtil.getUserHome();
		assertNotNull(dir);
		assertTrue(dir.exists());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testIS_JAVA() {
		final String javaVersion = SystemUtil.JAVA_VERSION;
		if (javaVersion == null) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("1.8")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertTrue(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("9")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertTrue(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("10")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertTrue(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("11")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertTrue(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("12")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertTrue(SystemUtil.IS_JAVA_12);
			assertFalse(SystemUtil.IS_JAVA_13);
		} else if (javaVersion.startsWith("13")) {
			assertFalse(SystemUtil.IS_JAVA_1_4);
			assertFalse(SystemUtil.IS_JAVA_1_5);
			assertFalse(SystemUtil.IS_JAVA_1_6);
			assertFalse(SystemUtil.IS_JAVA_1_7);
			assertFalse(SystemUtil.IS_JAVA_1_8);
			assertFalse(SystemUtil.IS_JAVA_9);
			assertFalse(SystemUtil.IS_JAVA_10);
			assertFalse(SystemUtil.IS_JAVA_11);
			assertFalse(SystemUtil.IS_JAVA_12);
			assertTrue(SystemUtil.IS_JAVA_13);
		} else {
			System.out.println("Can't test IS_JAVA value: " + javaVersion);
		}
	}

	@Test
	public void testIS_OS() {
		final String osName = System.getProperty("os.name");
		if (osName == null) {
			assertFalse(SystemUtil.IS_OS_WINDOWS);
			assertFalse(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_SOLARIS);
			assertFalse(SystemUtil.IS_OS_LINUX);
			assertFalse(SystemUtil.IS_OS_MAC_OSX);
		} else if (osName.startsWith("Windows")) {
			assertFalse(SystemUtil.IS_OS_UNIX);
			assertTrue(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.startsWith("Solaris")) {
			assertTrue(SystemUtil.IS_OS_SOLARIS);
			assertTrue(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.toLowerCase(Locale.ENGLISH).startsWith("linux")) {
			assertTrue(SystemUtil.IS_OS_LINUX);
			assertTrue(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.startsWith("Mac OS X")) {
			assertTrue(SystemUtil.IS_OS_MAC_OSX);
			assertTrue(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.startsWith("OS/2")) {
			assertTrue(SystemUtil.IS_OS_OS2);
			assertFalse(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.startsWith("SunOS")) {
			assertTrue(SystemUtil.IS_OS_SUN_OS);
			assertTrue(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else if (osName.startsWith("FreeBSD")) {
			assertTrue(SystemUtil.IS_OS_FREE_BSD);
			assertTrue(SystemUtil.IS_OS_UNIX);
			assertFalse(SystemUtil.IS_OS_WINDOWS);
		} else {
			System.out.println("Can't test IS_OS value: " + osName);
		}
	}

	@Test
	public void testIS_zOS() {
		final String osName = System.getProperty("os.name");
		if (osName == null) {
			assertFalse(SystemUtil.IS_OS_ZOS);
		} else if (osName.contains("z/OS")) {
			assertFalse(SystemUtil.IS_OS_WINDOWS);
			assertTrue(SystemUtil.IS_OS_ZOS);
		}
	}

	@Test
	public void testJavaVersionMatches() {
		String javaVersion = null;
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.4.0";
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.4.1";
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.4.2";
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.5.0";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.6.0";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.7.0";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "1.8.0";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
		javaVersion = "9";
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.4"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.5"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.6"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.7"));
		assertFalse(SystemUtil.isJavaVersionMatch(javaVersion, "1.8"));
		assertTrue(SystemUtil.isJavaVersionMatch(javaVersion, "9"));
	}

	@Test
	public void testIsJavaVersionAtLeast() {
		if (SystemUtil.IS_JAVA_1_8) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_9) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_10) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_11) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_12) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertFalse(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_13) {
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtLeast(JavaVersion.JAVA_13));
		}
	}

	@Test
	public void testIsJavaVersionAtMost() {
		if (SystemUtil.IS_JAVA_1_8) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_9) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_10) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_11) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_12) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		} else if (SystemUtil.IS_JAVA_13) {
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_4));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_5));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_6));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_7));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_1_8));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_9));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_10));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_11));
			assertFalse(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_12));
			assertTrue(SystemUtil.isJavaVersionAtMost(JavaVersion.JAVA_13));
		}
	}

	@Test
	public void testOSMatchesName() {
		String osName = null;
		assertFalse(SystemUtil.isOSNameMatch(osName, "Windows"));
		osName = "";
		assertFalse(SystemUtil.isOSNameMatch(osName, "Windows"));
		osName = "Windows 95";
		assertTrue(SystemUtil.isOSNameMatch(osName, "Windows"));
		osName = "Windows NT";
		assertTrue(SystemUtil.isOSNameMatch(osName, "Windows"));
		osName = "OS/2";
		assertFalse(SystemUtil.isOSNameMatch(osName, "Windows"));
	}

	@Test
	public void testOSMatchesNameAndVersion() {
		String osName = null;
		String osVersion = null;
		assertFalse(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "";
		osVersion = "";
		assertFalse(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "Windows 95";
		osVersion = "4.0";
		assertFalse(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "Windows 95";
		osVersion = "4.1";
		assertTrue(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "Windows 98";
		osVersion = "4.1";
		assertTrue(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "Windows NT";
		osVersion = "4.0";
		assertFalse(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
		osName = "OS/2";
		osVersion = "4.0";
		assertFalse(SystemUtil.isOSMatch(osName, osVersion, "Windows 9", "4.1"));
	}

	@Test
	public void testOsVersionMatches() {
		String osVersion = null;
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1"));

		osVersion = "";
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1"));

		osVersion = "10";
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10.1"));

		osVersion = "10.1";
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1.1"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.10"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.10.1"));

		osVersion = "10.1.1";
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.1.1"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.10"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.10.1"));

		osVersion = "10.10";
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10.1"));

		osVersion = "10.10.1";
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1"));
		assertFalse(SystemUtil.isOSVersionMatch(osVersion, "10.1.1"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10"));
		assertTrue(SystemUtil.isOSVersionMatch(osVersion, "10.10.1"));
	}
}