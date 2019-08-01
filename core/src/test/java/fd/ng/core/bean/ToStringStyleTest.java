package fd.ng.core.bean;

import fd.ng.core.utils.StringUtil;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ToStringStyleTest {
	private static class ToStringStyleImpl extends ToStringStyle {
		private static final long serialVersionUID = 1L;

	}

	//-----------------------------------------------------------------------
	@Test
	public void testSetArrayStart() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setArrayStart(null);
		assertEquals("", style.getArrayStart());
	}

	@Test
	public void testSetArrayEnd() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setArrayEnd(null);
		assertEquals("", style.getArrayEnd());
	}

	@Test
	public void testSetArraySeparator() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setArraySeparator(null);
		assertEquals("", style.getArraySeparator());
	}

	@Test
	public void testSetContentStart() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setContentStart(null);
		assertEquals("", style.getContentStart());
	}

	@Test
	public void testSetContentEnd() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setContentEnd(null);
		assertEquals("", style.getContentEnd());
	}

	@Test
	public void testSetFieldNameValueSeparator() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setFieldNameValueSeparator(null);
		assertEquals("", style.getFieldNameValueSeparator());
	}

	@Test
	public void testSetFieldSeparator() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setFieldSeparator(null);
		assertEquals("", style.getFieldSeparator());
	}

	@Test
	public void testSetNullText() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setNullText(null);
		assertEquals("", style.getNullText());
	}

	@Test
	public void testSetSizeStartText() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setSizeStartText(null);
		assertEquals("", style.getSizeStartText());
	}

	@Test
	public void testSetSizeEndText() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setSizeEndText(null);
		assertEquals("", style.getSizeEndText());
	}

	@Test
	public void testSetSummaryObjectStartText() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setSummaryObjectStartText(null);
		assertEquals("", style.getSummaryObjectStartText());
	}

	@Test
	public void testSetSummaryObjectEndText() {
		final ToStringStyle style = new ToStringStyleImpl();
		style.setSummaryObjectEndText(null);
		assertEquals("", style.getSummaryObjectEndText());
	}

	@Test
	public void testToStringStyle() {
		Person person = new Person();
		person.name = "张三"; person.age = 88; person.smoker = true;
		String toString_style;

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.SIMPLE_STYLE);
		assertThat(toString_style, equalTo("'张三',88,true"));
		assertThat(toString_style, not(containsString("Person")));

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.DEFAULT_STYLE);
		assertThat(toString_style, startsWith("fd.ng.core.bean.ToStringStyleTest$Person"));
		assertThat(toString_style, endsWith("[name='张三',age=88,smoker=true]"));

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.MULTI_LINE_STYLE);
		assertThat(toString_style, startsWith("fd.ng.core.bean.ToStringStyleTest$Person"));
		assertThat(toString_style, containsString("name='张三'"));
		assertThat(StringUtil.substringCount(toString_style, "\n"), is(4));

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.NO_FIELD_NAMES_STYLE);
		assertThat(toString_style, startsWith("fd.ng.core.bean.ToStringStyleTest$Person"));
		assertThat(toString_style, endsWith("'张三',88,true]"));

//		toString_style = ToStringBuilder.reflectionToString(
//				person, ToStringStyle.JSON_STYLE);
//		assertThat(toString_style, startsWith("{\"name\":"));

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.SHORT_PREFIX_STYLE);
		assertThat(toString_style, equalTo("ToStringStyleTest.Person[name='张三',age=88,smoker=true]"));

		toString_style = ToStringBuilder.reflectionToString(
				person, ToStringStyle.NO_CLASS_NAME_STYLE);
		assertThat(toString_style, equalTo("[name='张三',age=88,smoker=true]"));
	}
	static class Person {
		String name;
		int age;
		boolean smoker;
	}
}
