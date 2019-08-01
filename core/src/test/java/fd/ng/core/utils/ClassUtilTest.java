package fd.ng.core.utils;

import fd.ng.core.annotation.AnnoTest;
import fd.ng.core.utils.beans.*;
import fd.ng.core.utils.beans.classutil.OneBean;
import fd.ng.core.utils.beans.classutil.TwoBean;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ClassUtilTest extends FdBaseTestCase {
	@Ignore
	@Test
	public void testClassloader() {
		System.out.println("ClassUtil.class.getResource :");
		System.out.printf("%-25s=%s %n", "(\"\")", ClassUtil.class.getResource(""));
		System.out.printf("%-25s=%s %n", "(\"/\")", ClassUtil.class.getResource("/"));
		System.out.printf("%-25s=%s %n", "(\"log4j2.xml\")", ClassUtil.class.getResource("log4j2.xml"));
		System.out.printf("%-25s=%s %n", "(\"/log4j2.xml\")", ClassUtil.class.getResource("/log4j2.xml"));
		System.out.println();
		System.out.println("ClassUtil.class.getClassLoader().getResource :");
		System.out.printf("%-25s=%s %n", "(\"\")", ClassUtil.class.getClassLoader().getResource(""));
		System.out.printf("%-25s=%s %n", "(\"/\")", ClassUtil.class.getClassLoader().getResource("/"));
		System.out.printf("%-25s=%s %n", "(\"log4j2.xml\")", ClassUtil.class.getClassLoader().getResource("log4j2.xml"));
		System.out.printf("%-25s=%s %n", "(\"/log4j2.xml\")", ClassUtil.class.getClassLoader().getResource("/log4j2.xml"));
		System.out.println();
		System.out.printf("%-25s=%s %n", "getRuntimePath()", ClassUtil.getRuntimePath());
	}
	@Test
	public void getClassList() {
		Set<String> classNames;
		classNames = ClassUtil.getClassNamesByPackageName("");
		loopClassNames(classNames, "");
		classNames = ClassUtil.getClassNamesByPackageName("fd");
		loopClassNames(classNames, "fd");
	}
	// 本函数仅为上面的 getClassList 服务
	private void loopClassNames(Set<String> classNames, String packagename){
		for(String clsName : classNames){
			Class clz = null;
			try {
				clz = ClassUtil.loadClass(clsName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				fail(clsName+" not found!");
			}
			if(StringUtil.isNotEmpty(packagename))
				assertThat(clsName, startsWith(packagename));
			assertThat(clz, notNullValue());
			TestCaseLog.println("[OK   ] Load class : [ %s ]\n", clsName);
		}
	}

	@Test
	public void getClassListByAnnotation(){
		try {
			List<Class<?>> classList = ClassUtil.getClassListByAnnotation("fd.ng.core.utils.beans", AnnoTest.class);
			assertThat(classList, notNullValue());
			List<String> classNameList = classList.stream().map(Class::getName).collect(Collectors.toList());
			assertThat(classNameList, allOf(
					hasItem("fd.ng.core.utils.beans.SecondBaseClass"), hasItem("fd.ng.core.utils.beans.OtherClass"),
					not(hasItem("fd.ng.core.utils.beans.ThreeClass")),
					hasItem("fd.ng.core.utils.beans.tree.TreeOneClass"),
					not(hasItem("fd.ng.core.utils.beans.tree.TreeTwoClass"))
			));
		} catch (ClassNotFoundException e) {
			fail(e.toString());
		}
	}

	@Test
	public void getClassListBySuper() {
		try {
			List<Class<?>> classList = ClassUtil.getClassListBySuper("fd.ng.core.utils.beans", TestBaseClass.class);
			assertThat(classList, notNullValue());
			assertThat(classList.size(), is(2));
			List<String> classNameList = classList.stream().map(Class::getName).collect(Collectors.toList());
			assertThat(classNameList, allOf(
					hasItem("fd.ng.core.utils.beans.SecondBaseClass"),
					hasItem("fd.ng.core.utils.beans.ThreeClass"),
					not(hasItem("fd.ng.core.utils.beans.TestBaseClass")),
					not(hasItem("fd.ng.core.utils.beans.OtherClass")),
					not(hasItem("fd.ng.core.utils.beans.tree.TreeOneClass")),
					not(hasItem("fd.ng.core.utils.beans.tree.TreeTwoClass"))
			));
		} catch (ClassNotFoundException e) {
			fail(e.toString());
		}
	}

	@Test
	public void isSuperClass(){
		Assert.assertFalse("孙子类-->爷爷类", ClassUtil.isSuperClass(ThreeClass.class, TestBaseClass.class));
		Assert.assertTrue("爷爷类-->儿子类", ClassUtil.isSuperClass(TestBaseClass.class, SecondBaseClass.class));
		Assert.assertTrue("爷爷类-->孙子类", ClassUtil.isSuperClass(TestBaseClass.class, ThreeClass.class));
		Assert.assertTrue("父类-->儿子类", ClassUtil.isSuperClass(SecondBaseClass.class, ThreeClass.class));
		Assert.assertFalse("相同类（孙子）", ClassUtil.isSuperClass(ThreeClass.class, ThreeClass.class));
		Assert.assertFalse("相同类（爷爷）", ClassUtil.isSuperClass(TestBaseClass.class, TestBaseClass.class));

		Assert.assertFalse("其他类-->孙子类", ClassUtil.isSuperClass(OtherClass.class, ThreeClass.class));
		Assert.assertFalse("爷爷类-->其他类", ClassUtil.isSuperClass(TestBaseClass.class, OtherClass.class));
	}

	@Test
	public void propertyDescriptors() {
		PropertyDescriptor[] descriptors = ClassUtil.propertyDescriptors(ThreeClass.class);
		assertThat(descriptors.length, is(7));
	}
	/**
	 * 多次执行查看性能。
	 * 除第一次调用外，后面的调用都应该是 0ms
	 */
	@Ignore
	@Test
	public void testPerfCommnosPropertyDescriptors() {
		PropertyUtils.getPropertyDescriptors(OtherClass.class);
		PropertyUtils.getPropertyDescriptors(SecondBaseClass.class);
		PropertyUtils.getPropertyDescriptors(SomeClass.class);
		PropertyUtils.getPropertyDescriptors(ThreeClass.class);
	}
	@Ignore
	@Test
	public void testPerfJAVAPropertyDescriptors() {
		getPropertyDescriptors(OtherClass.class);
		getPropertyDescriptors(SecondBaseClass.class);
		getPropertyDescriptors(SomeClass.class);
		getPropertyDescriptors(ThreeClass.class);
	}
	private PropertyDescriptor[] getPropertyDescriptors(Class<?> clz) {
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(clz);
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		return beanInfo.getPropertyDescriptors();
	}

	@Test
	public void methodParams0() {
		methodParams();
		methodParams();
		methodParams();
		methodParams();
		methodParams();
	}
	@Test
	public void methodParams() {
		SomeClass someClass = new SomeClass();
		Method[] methods = someClass.getClass().getMethods();
		Method actionMethod = null;
		for(Method method : methods)
			if(method.getName().equalsIgnoreCase("hello"))
				actionMethod = method;
		Class<?>[] methodParamTypes = actionMethod.getParameterTypes();

		long start = System.currentTimeMillis();
		for(Class<?> clz : methodParamTypes) {
			String h = String.format("%22s, TypeName=%22s : ", clz.getName(), clz.getTypeName());
			String t = (h
					+ ", isPrimitive=" + clz.isPrimitive()
					+ ", isArray=" + clz.isArray()
					+ ", isString=" + String.class.isAssignableFrom(clz)
					+ ", isString[]=" + String[].class.isAssignableFrom(clz)
					+ ", isInt=" + int.class.isAssignableFrom(clz)
					+ ", isInt[]=" + int[].class.isAssignableFrom(clz)
					+ ", isInteger=" + Integer.class.isAssignableFrom(clz)
					+ ", isInteger[]=" + Integer[].class.isAssignableFrom(clz)
					+ ", isBigDecimal=" + BigDecimal.class.isAssignableFrom(clz)
					+ ", isMap=" + Map.class.isAssignableFrom(clz)
			);
			//System.out.println(t);
		}
		TestCaseLog.println("deal time : " + (System.currentTimeMillis()-start));
		Type[] gpt = actionMethod.getGenericParameterTypes();
		for(Type type : gpt) {
//			if(type.getClass() instanceof String)
		}
		Class<?> methodReturnType = actionMethod.getReturnType();
		Type grt = actionMethod.getGenericReturnType();

	}

	@Ignore("测试JDK各种方法该如何使用")
	@Test
	public void testAccess() throws IllegalAccessException {
		OneBean oneBean = new OneBean();
		oneBean.setName("n1"); oneBean.setAge(11); oneBean.setOnlyWrite(0); oneBean.setYy("yyyyyyyy");

		Field[] fieldsOneBean = OneBean.class.getDeclaredFields();
		for(Field field : fieldsOneBean) {
			String fieldName = field.getName();
			boolean v = field.isAccessible();
			try {
				Object o = field.get(oneBean);
				fail("不应该能被访问，因为都是 private 的字段");
			} catch (IllegalAccessException e) {
				field.setAccessible(true);
				Object o = field.get(oneBean);  // 应该可以得到每个 field 的值
				if("name".equals(fieldName))
					assertThat(o, Matchers.equalTo("n1"));
				if("age".equals(fieldName))
					assertThat(o, Matchers.equalTo(11));
				if("yyy".equals(fieldName))
					assertThat(o, Matchers.equalTo("yyyyyyyy"));
				if("xxx".equals(fieldName))
					assertThat(o, Matchers.equalTo(0L)); // 因为 xxx 这个field没有办法设置值，所以是默认值0
			}
			assertThat(v, is(false));
		}

		TwoBean twoBean = new TwoBean();
		twoBean.setName("two"); twoBean.setZip("zip"); twoBean.setOneProp("sdf");

		Field[] fieldsTwoBean = TwoBean.class.getDeclaredFields();
		for(Field field : fieldsTwoBean) {
			String fieldName = field.getName();
			boolean v = field.isAccessible();
			try {
				Object o = field.get(twoBean);
				fail("不应该能被访问，因为都是 private 的字段");
			} catch (IllegalAccessException e) {
				field.setAccessible(true);
				Object o = field.get(twoBean);  // 应该可以得到每个 field 的值
				if("name".equals(fieldName))
					assertThat(o, Matchers.equalTo("two"));
				if("age".equals(fieldName))
					fail("不能包含父类的field : age");
				if("zip".equals(fieldName))
					assertThat(o, Matchers.equalTo("zip"));
			}
			assertThat(v, is(false));
		}
	}

	@Test
	public void getAllFields() {
		final Field[] fieldsNumber = Number.class.getDeclaredFields();
		List<Field> numberList = new ArrayList<>();
		Collections.addAll(numberList, fieldsNumber);
		List<Field> listNumber = ClassUtil.getAllFields(Number.class);
		assertThat(listNumber, Matchers.equalTo(numberList));

		final Field[] fieldsInteger = Integer.class.getDeclaredFields();
		List<Field> integerList = new ArrayList<>();
		Collections.addAll(integerList, fieldsNumber);
		Collections.addAll(integerList, fieldsInteger);
		List<Field> listInteger = ClassUtil.getAllFields(Integer.class);
		assertThat(listInteger.size(), is(integerList.size()));
		assertThat(listInteger.containsAll(integerList), is(true));

		final Field[] fieldsOneBean = OneBean.class.getDeclaredFields();
		List<Field> listOneBean = new ArrayList<>();
		Collections.addAll(listOneBean, fieldsOneBean);
		List<Field> listOneBean0 = ClassUtil.getAllFields(OneBean.class);
		assertThat(listOneBean0, Matchers.equalTo(listOneBean));

		final Field[] fieldsTwoBean = TwoBean.class.getDeclaredFields();
		List<Field> listTwoBean = new ArrayList<>();
		Collections.addAll(listTwoBean, fieldsTwoBean);
		Collections.addAll(listTwoBean, fieldsOneBean);
		List<Field> listTwoBean0 = ClassUtil.getAllFields(TwoBean.class);
		assertThat(listTwoBean0.size(), is(listTwoBean.size()));
		assertThat(listTwoBean0.containsAll(listTwoBean), is(true));
	}

	@Test
	public void getPropertyDescriptorFields() {
		List<Field> fieldsOne = ClassUtil.getPropertyDescriptorFields(OneBean.class);
		fieldsOne.forEach(field -> {
			assertThat(field.getName(), anyOf(is("name"), is("age")));
		});

		List<Field> fieldsTwo = ClassUtil.getPropertyDescriptorFields(TwoBean.class);

		fieldsTwo.forEach(field -> {
			assertThat(field.getName(), anyOf(is("name"), is("age"), is("zip")));
		});
	}
}
