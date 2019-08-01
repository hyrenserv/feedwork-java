package fd.ng.core.utils;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class CsvUtilTest {

	@Test
	public void toCsv() {
		CsvWriter writer = new CsvWriter(new CsvWriterSettings());
		StringBuilder sb = new StringBuilder();

		String str = "hello";
		String csv = CsvUtil.toCsv(str, sb);
		assertThat(csv, Matchers.is("hello"));
		String upCsv = writer.writeRowToString(str);
		assertThat(csv, Matchers.is(upCsv));

		sb.delete(0, sb.length());
		str = "hello, world";
		csv = CsvUtil.toCsv(str, sb);
		assertThat(csv, Matchers.is("\"hello, world\""));
		upCsv = writer.writeRowToString(str);
		assertThat(csv, Matchers.is(upCsv));

		sb.delete(0, sb.length());
		str = "hello world \"";
		csv = CsvUtil.toCsv(str, sb);
		assertThat(csv, Matchers.is("\"hello world \"\"\""));
//		assertThat(csv, Matchers.is(writer.writeRowToString(str)));

		sb.delete(0, sb.length());
		str = "hello \nworld ";
		csv = CsvUtil.toCsv(str, sb);
		assertThat(csv, Matchers.is("\"hello \nworld \""));
//		upCsv = writer.writeRowToString(str);
//		assertThat(csv, Matchers.is(upCsv));

		sb.delete(0, sb.length());
		str = "hello \nworld \"";
		csv = CsvUtil.toCsv(str, sb);
		assertThat(csv, Matchers.is("\"hello \nworld \"\"\""));
//		upCsv = writer.writeRowToString(str);
//		assertThat(csv, Matchers.is(upCsv));
	}

	@Ignore("测试用哪种方式的性能好")
	@Test
	public void testPerf() {

		StringBuilder sb = new StringBuilder();
		String str1 = "hello world 水水电费水水电费水水电费电费, i like u \r\n\"over.\"";
		String str2 = "hello world 水水电费水水电费水水电费电费  i like u \r\nover.";
		int count = 20_000_000;

		CsvUtil.toCsv(str1, sb);
		toCsv(str1, sb);
		CsvWriter writer = new CsvWriter(new CsvWriterSettings());
		String row = writer.writeRowToString(str1);

		long start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			sb.delete(0, sb.length());
			CsvUtil.toCsv(str1, sb);
			sb.delete(0, sb.length());
			CsvUtil.toCsv(str2, sb);
		}
		System.out.println("loop char[] time : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			sb.delete(0, sb.length());
			toCsv(str1, sb);
			sb.delete(0, sb.length());
			toCsv(str2, sb);
		}
		System.out.println("use indexOf time : " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			writer.writeRowToString(str1);
			writer.writeRowToString(str2);
		}
		System.out.println("use uniPasr time : " + (System.currentTimeMillis() - start));
	}

	// 反复调用 indexOf 的方式，性能不好！
	private static String toCsv(final String str, final StringBuilder csvBuffer) {
		if(str.indexOf(CsvUtil.QUOTE)>-1) {
			// 有双引号，则替换成两个双引号后包裹上双引号返回
			return csvBuffer
					.append(CsvUtil.QUOTE)
					.append(str.replace(CsvUtil.QUOTE_STR, CsvUtil.ESCAPED_QUOTE_STR))
					.append(CsvUtil.QUOTE)
					.toString();
		} else if(str.indexOf(CsvUtil.DELIMITER)>-1||str.indexOf(StringUtil.LF)>-1||str.indexOf(StringUtil.CR)>-1) {
			return csvBuffer
					.append(CsvUtil.QUOTE)
					.append(str)
					.append(CsvUtil.QUOTE)
					.toString();
		}
		else
			return str;
	}
}