package fd.ng.core.utils;

import com.google.common.base.Splitter;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.RunTimeWatcher;
import fd.ng.test.junit.TestCaseLog;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class StringUtilTest extends FdBaseTestCase
{
	@Rule public RunTimeWatcher runTimeWatcher = new RunTimeWatcher();
	@Rule public TestName testName = new TestName();

	public enum WhichWay { All, MyOld, MyStrFaster, myCopyApache, MyGuava, JDK, ApacheV2, ApacheV3, Guava; }
	WhichWay runWhich = WhichWay.All; // 1: MyOld, 2:JDK, 3:Apache, 4:Guava
	boolean isTestPerformance = true;
	String hugeString = "/fd/687889/slkfj/fd/ng/234234/slkfj/XXXXXX/ng/util/sdl[{--]}kfj/0998/util/";
	public StringUtilTest(){
		if(isTestPerformance) {
			int _1_WAN = 7;
			int _15_WAN = 11;
			int _120_WAN = 14;
			int _2_QIAN_WAN = 18;
			for(int i=0; i<_15_WAN; i++)
				hugeString += hugeString;
		}
	}

	@Test
	public void getStrCount(){
		Assert.assertEquals(4, StringUtil.substringCount(
				"sdf 123 xxxx 123 yyyy123.class123", "123"));
		Assert.assertEquals(1, StringUtil.substringCount(
				"123sdf 23", "123"));
	}

	// 测试性能
	@Ignore
	@Test
	public void getStrCountPerf(){
		Assert.assertEquals(4, StringUtil.substringCount(
				"sdf 123 xxxx 123 yyyy123.class123", "123"));
		Assert.assertEquals(1, StringUtil.substringCount(
				"123sdf 23", "123"));
		String sourceStr = hugeString;
		System.out.printf("性能测试：src length : %,-5d\n", sourceStr.length());
		long start = 0, time = 0;
		start = System.currentTimeMillis();
		int result = StringUtil.substringCount(sourceStr, "/fd/");
		time = System.currentTimeMillis() - start;
		System.out.printf("time : %,-5d ms, count : %,-5d \n", time, result);
	}

	@Test
	public void replaceLast() {
		String sourceStr = "sdf 123 xxxx 123 yyyy 123.class";
		String result = null;
		result = StringUtil.replaceLast(sourceStr, "123", "{{NEW:FD}}");
		Assert.assertTrue(result.endsWith("{{NEW:FD}}.class"));
		Assert.assertEquals(2, StringUtil.substringCount(result, "123"));
		Assert.assertEquals(1, StringUtil.substringCount(result, "{{NEW:FD}}"));

		result = StringUtil.replaceLast(sourceStr, ".class", "{{NEW:FD}}");
		Assert.assertTrue(result.endsWith(" 123{{NEW:FD}}"));
		Assert.assertEquals(0, StringUtil.substringCount(result, "class"));
		Assert.assertEquals(1, StringUtil.substringCount(result, "{{NEW:FD}}"));
	}

	@Ignore
	@Test
	public void replaceLastPerf() {
		String sourceStr = "sdf 123 xxxx 123 yyyy 123.class";
		String result = null;
		sourceStr = hugeString + ".class";
		System.out.printf("性能测试：src length : %,d\n", sourceStr.length());
		long start = 0, time = 0;
//			src = sourceStr;

		start = System.currentTimeMillis();
		result = StringUtil.replaceLast(sourceStr, "/fd/", "{{NEW:FD}}");
		time = System.currentTimeMillis() - start;
		System.out.printf("被替换串在开头： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(0, 100));

		start = System.currentTimeMillis();
		result = StringUtil.replaceLast(sourceStr, ".class", "{{NEW:FD}}");
		time = System.currentTimeMillis() - start;
		System.out.printf("被替换串在结尾： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(result.length()<100?0:result.length()-100));
		System.out.printf("性能测试 结束！");
	}

	@Test
	public void replaceFirst() {
		String sourceStr = "sdf 123 xxSdfxx 123 yyyy 123.class";
		String result = null, repStr = "{{NEW:FD}}";

		result = StringUtil.replaceFirst(sourceStr, " 123", repStr);
		Assert.assertTrue(result.startsWith("sdf{{NEW:FD}} x"));
		Assert.assertEquals(2, StringUtil.substringCount(result, "123"));
		Assert.assertEquals(1, StringUtil.substringCount(result, repStr));

		result = StringUtil.replaceFirst(sourceStr, "sdf", repStr);
		Assert.assertTrue(result.startsWith("{{NEW:FD}} 123"));
		Assert.assertEquals(0, StringUtil.substringCount(result, "sdf"));
		Assert.assertEquals(1, StringUtil.substringCount(result, repStr));

		result = StringUtil.replaceFirst(sourceStr, "sdf");
		Assert.assertTrue(result.startsWith(" 123"));
		Assert.assertEquals(0, StringUtil.substringCount(result, "sdf"));

		result = StringUtil.replaceFirst(sourceStr, "xxS");
		Assert.assertTrue(result.startsWith("sdf 123 df"));
		Assert.assertEquals(0, StringUtil.substringCount(result, "xxS"));
	}

	@Ignore
	@Test
	public void replaceFirstPerf() {
		String sourceStr = "sdf 123 xxSdfxx 123 yyyy 123.class";
		String result = null, repStr = "{{NEW:FD}}";
		sourceStr = hugeString + ".class";
		System.out.printf("性能测试：src length : %,5d\n", sourceStr.length());
		long start = 0, time = 0;
		//repStr="";
		// MyOld
		start = System.currentTimeMillis();
		result = StringUtil.replaceFirst(sourceStr, "/fd/", repStr);
		time = System.currentTimeMillis() - start;
		System.out.printf("[MyOld ] 被替换串在开头： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(0, 100));
		// JDK
		start = System.currentTimeMillis();
		result = sourceStr.replaceFirst("/fd/", repStr);
		time = System.currentTimeMillis() - start;
		System.out.printf("[JDK] 被替换串在开头： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(0, 100));

		// MyOld
		start = System.currentTimeMillis();
		result = StringUtil.replaceFirst(sourceStr, ".class", repStr);
		time = System.currentTimeMillis() - start;
		System.out.printf("[MyOld ] 被替换串在结尾： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(result.length()<100?0:result.length()-100));
		// JDK
		start = System.currentTimeMillis();
		result = sourceStr.replaceFirst(".class", repStr);
		time = System.currentTimeMillis() - start;
		System.out.printf("[JDK] 被替换串在结尾： time : %,-5d ms, v2 str : %s \n", time
				, result.substring(result.length()<100?0:result.length()-100));

		System.out.printf("性能测试 结束！");
	}

	@Test
	public void split() {
		List<String> splitResult;
		splitResult = StringUtil.split(null, "*");
		assertThat(splitResult.size(), is(0));

		splitResult = StringUtil.split("", "*");
		assertThat(splitResult.size(), is(0));

		splitResult = StringUtil.split("ab:cd:ef", ":");
		assertThat(splitResult.size(), is(3));
		assertThat(splitResult.get(0), is("ab"));
		assertThat(splitResult.get(1), is("cd"));
		assertThat(splitResult.get(2), is("ef"));

		splitResult = StringUtil.split("| |  lk|j| | ", "| ");
		assertThat(splitResult.size(), is(5));
		assertThat(splitResult.get(2), is(" lk|j"));
	}

	@Ignore
	@Test
	public void indexOfPerformance() {
		long start=0, time=0;

		// 大循环的测试
		int count = 90000000;
		int func = 2;
		System.out.printf("大循环性能测试：count : %,d\n", count);
		String string = "/fd//fd/数字132//fd/ng/2345/sl/fd/kfj/三个X/fd/XXX/ng/u/fd/til/sdl[{--]}/fd//0998/工具//fd//fd//fd//fd//fd/\n";
		int loc = -1; boolean has = false;
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			if( func == 1 )
				loc = string.indexOf("\n");
			else
				has = string.contains("\n");
		}
		time = System.currentTimeMillis() - start;
		if( func == 1 )
			System.out.printf("indexOf     time : %5d,   fisrtLoc : %d\n", time, loc);
		else
			System.out.printf("contains    time : %5d,   found it : %s\n", time, has);

		if(true) return;
		// 大串的测试
		String sourceStr = hugeString + ".class";
		System.out.printf("大串性能测试：src length : %5d\n", sourceStr.length());

		start = System.currentTimeMillis();
		int fisrtLoc = sourceStr.indexOf("/fd/"); // 出现开始
		time = System.currentTimeMillis() - start;
		System.out.printf("indexOf     time : %5d,   fisrtLoc : %d\n", time, fisrtLoc);
		start = System.currentTimeMillis();
		int fisrtLoc2 = sourceStr.indexOf("class"); // 出现在最后
		time = System.currentTimeMillis() - start;
		System.out.printf("indexOf     time : %5d,  fisrtLoc2 : %d\n", time, fisrtLoc2);

		start = System.currentTimeMillis();
		int lastLoc = sourceStr.lastIndexOf("/fd/");
		time = System.currentTimeMillis() - start;
		System.out.printf("lastIndexOf time : %5d,    lastLoc : %d\n", time, lastLoc);
		// last串在最开头
		sourceStr = "head" + sourceStr;
		start = System.currentTimeMillis();
		int lastLoc2 = sourceStr.lastIndexOf("head");
		time = System.currentTimeMillis() - start;
		System.out.printf("lastIndexOf time : %5d,   lastLoc2 : %d\n", time, lastLoc2);
		// last串在最开头，二分查找
		start = System.currentTimeMillis();
		int srcLen = sourceStr.length();
		String srcHead = sourceStr.substring(0, srcLen/2);
		String srcTail = sourceStr.substring(srcLen/2);
		int lastLocHead = srcHead.lastIndexOf("head");
		int lastLocTail = srcTail.lastIndexOf("head");
		time = System.currentTimeMillis() - start;
		System.out.printf("lastIndexOf time : %5d,   lastLoc3 : %d, %d\n", time, lastLocHead, lastLocTail);

		start = System.currentTimeMillis();
		boolean isEndsWith = sourceStr.endsWith(".class");
		time = System.currentTimeMillis() - start;
		System.out.printf("endsWith    time : %5d, isEndsWith : %s\n", time, isEndsWith);
	}

	@Ignore
	@Test
	public void append_indexof() {
		StringBuilder sber = new StringBuilder();
		String columndata = "/fd//fd/数字132//fd/ng/2345/sl/fd/kfj/三个X/fd/XXX/ng/u/fd/til/sdl[{--]}/fd//0998/工具//fd//fd//fd//fd//fd/";
		long start=0, time=0;
		int count = 30000000;

		// 验证append的性能
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			if(sber.length()>0) sber.delete(0, sber.length()-1);
			sber.append('"').append(columndata).append('"');
		}
		time = System.currentTimeMillis() - start;
		System.out.printf("[append ] time : %5d\n", time);
		// 验证indexof的性能
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			if(sber.length()>0) sber.delete(0, sber.length()-1);
			if(columndata.indexOf('"')>-1) {
				sber.append('"').append(columndata).append('"');
			}
		}
		time = System.currentTimeMillis() - start;
		System.out.printf("[contains] time : %5d\n", time);
		// 验证contains的性能
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			if(sber.length()>0) sber.delete(0, sber.length()-1);
			if(columndata.contains("\"")) {
				sber.append('"').append(columndata).append('"');
			}
		}
		time = System.currentTimeMillis() - start;
		System.out.printf("[contains] time : %5d\n", time);
	}

	// 测试 	contains 的性能
	@Ignore
	@Test
	public void strcon() {
		long start=0, time=0;
		int count = 300000000;
		int type = 1;
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			checkType("xxxint23klj43kjlkjl35kjl53kjl5");
		}
		time = System.currentTimeMillis() - start;
		System.out.printf("[checkType] time : %5d\n", time);
	}
	private void checkType(String str) {
		str = str.toLowerCase();
		if( str.contains("int") || str.contains("integer") || str.contains("smallint") || str.contains("tinyint") || str.contains("bigint")
				|| str.contains("real") || str.contains("float") || str.contains("decimal") || str.contains("number")
				|| str.contains("numeric")|| str.contains("date") ) {
			//
		}else{
			//
		}
	}
	String string = null;
	String sepStr = null;
	/**
	 * 对小串连续三次调split，看结果。性能测试使用
	 */
	@Ignore
	@Test
	public void splitTree(){
		TestCaseLog.println("%n=================== 第一次 ===================");
		string = "/fd//fd/数字132//fd/ng/2345/sl/fd/kfj/三个X/fd/XXX/ng/u/fd/til/sdl[{--]}/fd//0998/工具//fd//fd//fd//fd//fd/";
		sepStr = "/fd/";
		splitManyTimes();
		TestCaseLog.println("%n=================== 第二次 ===================");
		string = "1:2:3:4:5"; sepStr=":";
		splitManyTimes();
		TestCaseLog.println("%n=================== 第三次 ===================");
		string = "||||"; sepStr="|";
		splitManyTimes();
		// 2 3 4 5
		// 43553	5399	17688
		// 17860	5678	3652
		// 16312	6741	6837
		// 789		1460	678
	}
	@Ignore
	@Test
	public void splitManyTimes(){
		runWhich = WhichWay.All; // 2 3 4 5
		int count = 1; // 循环次数
		if(isTestPerformance) count = 5_000_000;
		if(string==null) string = "| | lk|j| | ";
		if(sepStr==null) sepStr = "| ";
		long start=0, time=0;
//		String string = "/fd//fd/687889//fd/ng/"+System.currentTimeMillis()+"/sl/fd/kfj/XXX/fd/XXX/ng/u/fd/til/sdl[{--]}/fd//0998/util//fd//fd//fd//fd//fd/";
//		string = "1:2:3:4:5"; sepStr=":";
		if(runWhich==WhichWay.JDK||runWhich==WhichWay.All) {
			String[] newStr = null;
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = string.split(sepStr);
			time = System.currentTimeMillis() - start;
			String newStrValue = Arrays.stream(newStr).collect(Collectors.joining(", "));
			int outLen = newStrValue.length()<100?newStrValue.length():100;
			System.out.printf("[JDK] time : %5d, newstr size : (%3d), (%s)\n", time, newStr.length, newStrValue.substring(0, outLen));
			newStr = null;
		}
		if(runWhich==WhichWay.ApacheV3||runWhich==WhichWay.All) {
			String[] newStr = null;
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens(string, sepStr);
			time = System.currentTimeMillis() - start;
			String newStrValue = Arrays.stream(newStr).collect(Collectors.joining(", "));
			int outLen = newStrValue.length()<100?newStrValue.length():100;
			System.out.printf("[AP3] time : %5d, newstr size : (%3d), (%s)\n", time, newStr.length, newStrValue.substring(0, outLen));
			newStr = null;
		}
		if(runWhich==WhichWay.ApacheV2||runWhich==WhichWay.All) {
			String[] newStr = null;
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = org.apache.commons.lang.StringUtils.splitByWholeSeparatorPreserveAllTokens(string, sepStr);
			time = System.currentTimeMillis() - start;
			String newStrValue = Arrays.stream(newStr).collect(Collectors.joining(", "));
			int outLen = newStrValue.length()<100?newStrValue.length():100;
			System.out.printf("[AP2] time : %5d, newstr size : (%3d), (%s)\n", time, newStr.length, newStrValue.substring(0, outLen));
			newStr = null;
		}
		if(runWhich==WhichWay.myCopyApache ||runWhich==WhichWay.All) {
			List<String> newStr = null;
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++) {
				newStr = StringUtil.split(string, sepStr);
				for(String s : newStr) { String s0 = s; }
			}
			time = System.currentTimeMillis() - start;
			String newStrValue = newStr.stream().collect(Collectors.joining(", "));
			int outLen = newStrValue.length()<100?newStrValue.length():100;
			System.out.printf("[MYA] time : %5d, newstr size : (%3d), (%s)\n", time, newStr.size(), newStrValue.substring(0, outLen));
			newStr = null;
		}
//		if(runWhich==WhichWay.MyStrFaster||runWhich==WhichWay.All) {
//			List<String> newStr = null;
//			StringFaster sfString = new StringFaster(string);
//			StringFaster sfSep = new StringFaster(sepStr);
//			start = System.currentTimeMillis();
//			for(int i=0; i<count; i++) {
//				newStr = StringUtil.splitFaster(sfString, sfSep);
//				for(String s : newStr) { String s0 = s; }
//			}
//			time = System.currentTimeMillis() - start;
//			String newStrValue = newStr.stream().collect(Collectors.joining(", "));
//			int outLen = newStrValue.length()<100?newStrValue.length():100;
//			System.out.printf("[MYF] time : %5d, newstr size : (%3d), (%s)\n", time, newStr.size(), newStrValue.substring(0, outLen));
//			newStr = null;
//		}
		if(runWhich==WhichWay.Guava||runWhich==WhichWay.All) {
			Iterable<String> newStrIt=null;
			Splitter splitter = Splitter.on(sepStr);
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++) {
				newStrIt = splitter.split(string);
				for(String s : newStrIt) { String s0 = s; }
			}
			time = System.currentTimeMillis() - start;
			String newStrValue = ""; int length = 0;
			for (Iterator iter = newStrIt.iterator(); iter.hasNext();) {
				newStrValue += iter.next() + ", ";
				length++;
			}
			newStrValue = newStrValue.substring(0, newStrValue.lastIndexOf(", "));
			int outLen = newStrValue.length()<100?newStrValue.length():100;
			System.out.printf("[GVA] time : %5d, newstr size : (%3d), (%s)\n", time, length, newStrValue.substring(0, outLen));
		}
	}

	@Ignore
	@Test
	public void splitBigStringTree(){
		String lastHugeString = hugeString;

		TestCaseLog.println("%n=================== 第一次 ===================");
		hugeString = lastHugeString + "/fd/" + System.currentTimeMillis();
		sepStr = "/fd/";
		splitBigString();

		TestCaseLog.println("%n=================== 第二次 ===================");
		hugeString = "/---/" + System.currentTimeMillis() + lastHugeString + "sdfdsf";
		sepStr = "--";
		splitBigString();

		TestCaseLog.println("%n=================== 第三次 ===================");
		hugeString = "////////" + System.currentTimeMillis() +  lastHugeString + "////////" + "////////";
		sepStr = "////////";
		splitBigString();
	}

	@Ignore
	@Test
	public void splitBigString(){
		System.out.println("\n********** splitBigString() Test : hugeString length="+hugeString.length()+" **********");
//		src = "/fd/687889/sdlkfj/fd/ng/234234/sdlkfj/9/";
//		final String sepStr = "/fd/";
		long start=0, time=0;
		if(runWhich==WhichWay.JDK||runWhich==WhichWay.All) {
			start = System.currentTimeMillis();
			String[] newStr = hugeString.split(sepStr);
			time = System.currentTimeMillis() - start;
			String newStrValue = Arrays.stream(newStr).collect(Collectors.joining(","));

			start = System.currentTimeMillis();
			List<String> newStrList = new ArrayList<>();
			for (String val : newStr)
				newStrList.add(val);
			long itMakeArrTime = System.currentTimeMillis() - start;

			int outLen = newStrValue.length()<30?newStrValue.length():30;
			System.out.printf("[JDK] time : %5d, newstr size : (%3d), (%s), %d\n",
					time, newStr.length, newStrValue.substring(0, outLen), itMakeArrTime);
			newStr = null;
		}
		if(runWhich==WhichWay.ApacheV3||runWhich==WhichWay.All) {
			start = System.currentTimeMillis();
			String[] newStr = org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens(hugeString, sepStr);
			time = System.currentTimeMillis() - start;
			String newStrValue = Arrays.stream(newStr).collect(Collectors.joining(","));

			start = System.currentTimeMillis();
			List<String> newStrList = new ArrayList<>();
			for (String val : newStr)
				newStrList.add(val);
			long itMakeArrTime = System.currentTimeMillis() - start;

			int outLen = newStrValue.length()<30?newStrValue.length():30;
			System.out.printf("[AP3] time : %5d, newstr size : (%3d), (%s), %d\n",
					time, newStr.length, newStrValue.substring(0, outLen), itMakeArrTime);
			newStr = null;
		}
		if(runWhich==WhichWay.Guava||runWhich==WhichWay.All) {
			start = System.currentTimeMillis();
			Iterable<String> newStrIt = Splitter.on(sepStr).split(hugeString);
			time = System.currentTimeMillis() - start;

//			start = System.currentTimeMillis();
//			int length = 0;
//			for (Iterator<String> iter = newStrIt.iterator(); iter.hasNext();iter.next())
//				length++;
//			long iteratorTime = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			List<String> newStrList = new ArrayList<>();
			for (Iterator<String> iter = newStrIt.iterator(); iter.hasNext();)
				newStrList.add(iter.next());
			long itMakeArrTime = System.currentTimeMillis() - start;

			String newStrValue = newStrList.stream().collect(Collectors.joining(","));

//			String newStrValue = "";
//			for (Iterator<String> iter = newStrIt.iterator(); iter.hasNext();) {
//				newStrValue += iter.next() + ", ";
//			}
//			newStrValue = newStrValue.substring(0, newStrValue.lastIndexOf(", "));

			int outLen = newStrValue.length()<30?newStrValue.length():30;
			System.out.printf("[GVA] time : %5d, newstr size : (%3d), (%s), %d\n",
					time, newStrList.size(), newStrValue.substring(0, outLen), itMakeArrTime);
		}
//		if(runWhich==WhichWay.MyGuava||runWhich==WhichWay.All) {
//			start = System.currentTimeMillis();
//			Iterable<String> newStrIt = StringUtil.split(hugeString, sepStr);
//			time = System.currentTimeMillis() - start;
//
//			start = System.currentTimeMillis();
//			List<String> newStrList = new ArrayList<>();
//			for (Iterator<String> iter = newStrIt.iterator(); iter.hasNext();)
//				newStrList.add(iter.next());
//			long itMakeArrTime = System.currentTimeMillis() - start;
//
//			String newStrValue = newStrList.stream().collect(Collectors.joining(","));
//
//			int outLen = newStrValue.length()<30?newStrValue.length():30;
//			System.out.printf("[MyS] time : %5d, newstr size : (%3d), (%s), %d\n",
//					time, newStrList.size(), newStrValue.substring(0, outLen), itMakeArrTime);
//		}
	}

	/**
	 * 判断字符串是否为数字
	 */
	@Test
	public void isNumeric2(){

		assertThat(StringUtil.isNumeric2("sdf"), is(false));
		assertThat(StringUtil.isNumeric2("10000"), is(true));
	}

	@Test
	public void fileSizeConversion(){

		assertThat(StringUtil.fileSizeConversion(1024L), is("1 KB"));
		assertThat(StringUtil.fileSizeConversion(1048576L), is("1 MB"));
		assertThat(StringUtil.fileSizeConversion(1073741824L), is("1 GB"));

	}
	// 测试对小串的多次替换的效率
	// 结论：commons-lang 2.6的性能竟然是最好的，奇怪了
	@Ignore
	@Test
	public void replaceMulti() {
		String str = "/fd/687889/\nsdlkfj/fd/\nng/234234/\nsdlkfj/9/\n";
		System.out.println("source string : [" + str + "]\n");

		replace(str, 1);
		replace(str, 90000000);
//		replace(str, 9000000);
//		replace(str, 90000000);
		// 1		9009		94		640		6459	char
		// 1		10606		101		713		7174	String
		// 2		41979		390		3880	85342
		// 3		13254		112		1150	11348
		// 4		10883		76		873		11807
	}
	private void replace(String str, int count){
		System.out.println("\n========>>>>>> replace count : " + count);
		//String src = "/fd/687889/sdlkfj/fd/ng/234234/sdlkfj/9/";
		runWhich = WhichWay.JDK;

		String serStr = "\n", repStr = " ";
		long start=0, time=0; String newStr=null;

		if(runWhich==WhichWay.ApacheV2||runWhich==WhichWay.All) {
			org.apache.commons.lang.StringUtils.replace(str, serStr, repStr);
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = org.apache.commons.lang.StringUtils.replace(str, serStr, repStr);
			time = System.currentTimeMillis() - start;
			int outLen = newStr.length()<50?newStr.length():50;
			System.out.printf("[AP2] replace %-9d time : %4d, newstr (%s)\n", count, time, newStr.substring(0, outLen));
		}
//		if(runWhich==WhichWay.MyOld||runWhich==WhichWay.All) {
//			StringUtil.replace(str, serStr, repStr);
//			StringFaster sb = new StringFaster(160);
//			start = System.currentTimeMillis();
//			for(int i=0; i<count; i++)
//				newStr = StringUtil.replace(str, serStr, repStr);
////				newStr = StringUtil.replaceInLoops(str, '\n', ' ', sb);
//			time = System.currentTimeMillis() - start;
//			int outLen = newStr.length()<50?newStr.length():50;
//			System.out.printf("[My ] replace %-9d time : %4d, newstr (%s)\n", count, time, newStr.substring(0, outLen));
//		}
		if(runWhich==WhichWay.JDK||runWhich==WhichWay.All) {
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = str.replaceAll(serStr, repStr);
//				newStr = str.replace('\n', ' ');
			time = System.currentTimeMillis() - start;
			int outLen = newStr.length()<50?newStr.length():50;
			System.out.printf("[JDK] replace %-9d time : %4d, newstr (%s)\n", count, time, newStr.substring(0, outLen));
		}
		if(runWhich==WhichWay.ApacheV3||runWhich==WhichWay.All) {
			org.apache.commons.lang3.StringUtils.replace(str, serStr, repStr);
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++)
				newStr = org.apache.commons.lang3.StringUtils.replace(str, serStr, repStr);
			time = System.currentTimeMillis() - start;
			int outLen = newStr.length()<50?newStr.length():50;
			System.out.printf("[AP3] replace %-9d time : %4d, newstr (%s)\n", count, time, newStr.substring(0, outLen));
		}
	}
}
