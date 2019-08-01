package fd.ng.db.resultset.helper;

import fd.ng.core.utils.ClassUtil;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.hamcrest.Matchers;
import org.junit.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResultSetToBeanHelperTest extends FdBaseTestCase {
	public static final String testTableName = "__testrstobean_990";
	private static final int Init_Rows = 15; // 初始插入的数据条数。必须 >=10，否则会导致后面的测试用例失败

	@BeforeClass
	public static void start() {
		TestCaseLog.println("Start ResultSetToEntityHelperTest ......");
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			if (!db.isExistTable(testTableName))
				db.ExecDDL("create table " + testTableName
						+ "(name varchar(48), age int, password varchar(20), class char(5), number int)");
		}
	}

	@Before
	public void before() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			List<Object[]> params = new ArrayList<>();
			for (int i = 0; i < Init_Rows; i++) {
				Object[] rowParams = new Object[]{"newUser" + i, i, "123", "Init"+(i%10)};
				params.add(rowParams);
			}
			int[] nums = SqlOperator.executeBatch(db,
					"insert into " + testTableName + "(name, age, password, class) values(?, ?, ?, ?)",
					params
			);
			assertThat("initData", nums.length, is(Init_Rows));
			for (int i = 0; i < nums.length; i++)
				assertThat("initData : " + i, nums[i], is(1));
			SqlOperator.commitTransaction(db);
		}
		TestCaseLog.println("Running One TestCase ... ...");
	}
	@After
	public void after() {
		TestCaseLog.println("Current TestCase Done, clean test data ... ...");
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			SqlOperator.execute(db,"delete from " + testTableName);
			long nums = SqlOperator.queryNumber(db, "select count(1) from " + testTableName)
					.orElseThrow(()->new RuntimeException("count fail!"));
			assertThat("整个表数据删除后，表记录数应该为0", nums, is(0L));
			SqlOperator.commitTransaction(db);
		}
	}
	@AfterClass
	public static void end() {
		TestCaseLog.println("Over  SqlOperatorTest......");
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			db.ExecDDL("drop table " + testTableName);
		}
	}

	@Test
	public void toBean() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long start = System.nanoTime();
			Optional<OneBean> result = SqlOperator.queryOneObject(db, OneBean.class,
					"select * from " + testTableName + " where age=?",
					5
			);
			long time = System.nanoTime() - start;
			// 不提取 Opetional 总的对象，直接使用
			assertThat(result.map(OneBean::getName).get(), is("newUser5"));
			assertThat(result.map(OneBean::getAge).get(), is(5));
			// 提取对象后做处理
			OneBean user = result.orElseGet(OneBean::new); // 没有查询到数据，则会new出来一个新对象。注：orElseGet是延时调用的方式
			assertThat(user.getName(), is("newUser5"));
			assertThat(user.getAge(), is(5));

			// 测试查询不到数据
			Optional<OneBean> resultNon = SqlOperator.queryOneObject(db, OneBean.class,
					"select age,name from " + testTableName + " where age=?",
					123456789
			);
			assertThat(resultNon.map(OneBean::getName).orElse("no user"), is("no user"));
			OneBean userNon = resultNon.orElseGet(OneBean::new);
			assertThat(userNon.getName(), Matchers.nullValue());

			start = System.nanoTime();
			Optional<OneBean> result0 = SqlOperator.queryOneObject(db, OneBean.class,
					"select * from " + testTableName + " where age=?",
					5
			);
			long time0 = System.nanoTime() - start;
			// 第一查询中，在BeanUtil里面构造OneBean的缓存，所以，耗时应该大于第二次
			TestCaseLog.println("单独执行时，time1应该远大于time2。但是，如果所有用例一起跑，就没准了，因为缓存已经提前被别的用例构建好了");
			TestCaseLog.println("time1="+time);
			TestCaseLog.println("time2="+time0);
		}
	}

	@Test
	public void toBean2() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Optional<TwoBean> result = SqlOperator.queryOneObject(db, TwoBean.class,
					"select * from " + testTableName + " where age=?",
					5
			);
			// 不提取 Opetional 总的对象，直接使用
			assertThat(result.map(TwoBean::getName).get(), is("newUser5"));
			assertThat(result.map(TwoBean::getAge).get(), is(5));
			assertThat(result.map(TwoBean::getNums).get(), is(Integer.MIN_VALUE));
		}
	}

	@Test
	public void toBeanList() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			List<OneBean> result = SqlOperator.queryList(db, OneBean.class,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.size(), is(9));
			assertThat(result.get(0).getName(), is("newUser9"));
			assertThat(result.get(1).getAge(), is(8));
			assertThat(result.get(2).getUclass(), is("Init7"));
		}
	}

	@Test
	public void toPagedBeanList() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Page page = new DefaultPageImpl(2, 4);
			List<OneBean> result = SqlOperator.queryPagedList(db, OneBean.class, page,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(page.getTotalSize(), is(9));
			assertThat(page.getPageCount(), is(3));
			assertThat(result.size(), is(4));
			assertThat(result.get(0).getName(), is("newUser5"));
			assertThat(result.get(1).getAge(), is(4));
			assertThat(result.get(2).getUclass(), is("Init3"));
		}
	}

	@Test
	public void toEntity() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			long start = System.nanoTime();
			Optional<OneEntity> result = SqlOperator.queryOneObject(db, OneEntity.class,
					"select * from " + testTableName + " where age=?",
					5
			);
			long time = System.nanoTime() - start;
			// 不提取 Opetional 总的对象，直接使用
			assertThat(result.map(OneEntity::getName).get(), is("newUser5"));
			assertThat(result.map(OneEntity::getAge).get(), is(5));
			// 提取对象后做处理
			OneEntity user = result.orElseGet(OneEntity::new); // 没有查询到数据，则会new出来一个新对象。注：orElseGet是延时调用的方式
			assertThat(user.getName(), is("newUser5"));
			assertThat(user.getAge(), is(5));

			// 测试查询不到数据
			Optional<OneEntity> resultNon = SqlOperator.queryOneObject(db, OneEntity.class,
					"select age,name from " + testTableName + " where age=?",
					123456789
			);
			assertThat(resultNon.map(OneEntity::getName).orElse("no user"), is("no user"));
			OneEntity userNon = resultNon.orElseGet(OneEntity::new);
			assertThat(userNon.getName(), Matchers.nullValue());
		}
	}

	@Test
	public void toEntity2() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			Optional<TwoEntity> result = SqlOperator.queryOneObject(db, TwoEntity.class,
					"select * from " + testTableName + " where age=?",
					5
			);
			// 不提取 Opetional 总的对象，直接使用
			assertThat(result.map(TwoEntity::getName).get(), is("newUser5"));
			assertThat(result.map(TwoEntity::getAge).get(), is(5));
			assertThat(result.map(TwoEntity::getNumber).get(), is(Integer.MIN_VALUE));
		}
	}

	@Test
	public void toEntityList() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			List<OneEntity> result = SqlOperator.queryList(db, OneEntity.class,
					"select * from " + testTableName + " where age>? and age<? order by age desc",
					0, 10
			);
			assertThat(result.size(), is(9));
			assertThat(result.get(0).getName(), is("newUser9"));
			assertThat(result.get(1).getAge(), is(8));
			assertThat(result.get(2).getUclass(), is("Init7"));
		}
	}

	@Ignore("用于观察查询耗时情况")
	@Test
	public void watchTime() {
		try (DatabaseWrapper db = new DatabaseWrapper()) {
			for(int i=0; i<1000; i++) {
				long start = System.currentTimeMillis();
				Optional<OneEntity> result = SqlOperator.queryOneObject(db, OneEntity.class,
						"select * from " + testTableName + " where age=?",
						5
				);
				long time = System.currentTimeMillis() - start;
				TestCaseLog.println("queryOneObject %4d : %d", (i+1), time);
			}
		}
	}

	@Ignore("用于观察获取属性和字段信息的公共函数")
	@Test
	public void propAnno() throws Exception {
		TwoEntity twoEntity = new TwoEntity();
		Class ownerClass = Class.forName("fd.ng.db.resultset.helper.TwoEntity");

		BeanInfo beanInfo = Introspector.getBeanInfo(ownerClass);
		PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < props.length; i++) {
			PropertyDescriptor prop = props[i];
			String name = prop.getName();
			Field field = ClassUtil.getFieldThroughSuper(ownerClass, name);
			Method r = prop.getReadMethod();
			Method w = prop.getWriteMethod();
			int j = i;
		}

		ClassUtil.getAllVisibleFields(OneBean.class);
		long start = System.currentTimeMillis();
		List<Field> fields = ClassUtil.getAllVisibleFields(ownerClass);
		System.out.println("getDeclaredFields : " + (System.currentTimeMillis()-start));
		fields.forEach(field -> {
			String name = field.getName();
			Annotation[] annos = field.getAnnotations();
			field.toGenericString();
		});

	}
	// ---------------- 以下用于测试两种反射的性能
	@Ignore("用于测试两种反射的性能")
	@Test
	public void testMethodCall()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
		Class ownerClass = Class.forName("fd.ng.db.resultset.helper.OneEntity");
		Object user0 = ownerClass.newInstance();

		int count = 100000; // 循环次数
		String nameValue = "fd";
		long start = -1;
		Object user = ownerClass.newInstance();

		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			Method setterN = ownerClass.getMethod("setName", String.class);
			setterN.invoke(user, "getm " + nameValue + i);
			Method setterA = ownerClass.getMethod("setAge", int.class);
			setterA.invoke(user, i);
//			System.out.println("getm user : " + user.toString());
		}
		System.out.println("getm : " + (System.currentTimeMillis()-start));
	}

	@Ignore("用于测试两种反射的性能")
	@Test
	public void testPropCall()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
		Class ownerClass = Class.forName("fd.ng.db.resultset.helper.OneEntity");
		Object user0 = ownerClass.newInstance();

		int count = 100000; // 循环次数
		String nameValue = "fd";
		long start = -1;
		Object user = ownerClass.newInstance();

		PropertyDescriptor propN = getPropertyDescriptor(ownerClass, "name");
		PropertyDescriptor propA = getPropertyDescriptor(ownerClass, "age");
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
			Method setterN = propN.getWriteMethod();
			setterN.invoke(user, "prop " + nameValue + i);
			Method setterA = propA.getWriteMethod();
			setterA.invoke(user, i);
//			System.out.println("prop user : " + user.toString());
		}
		System.out.println("prop : " + (System.currentTimeMillis()-start));
	}

	@Ignore("用于测试两种反射的性能")
	@Test
	public void testGetPropertyType()
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
		Class ownerClass = Class.forName("fd.ng.db.resultset.helper.OneEntity");
		Object user0 = ownerClass.newInstance();

		int count = 100000; // 循环次数
		String nameValue = "fd";
		long start = -1;

		PropertyDescriptor propN = getPropertyDescriptor(ownerClass, "name");
		PropertyDescriptor propA = getPropertyDescriptor(ownerClass, "age");
		PropertyDescriptor propP = getPropertyDescriptor(ownerClass, "password");
		PropertyDescriptor propC = getPropertyDescriptor(ownerClass, "create_time");
		start = System.currentTimeMillis();
		for(int i=0; i<count; i++) {
//			Object user = ownerClass.newInstance();
			Class c = propN.getPropertyType();
			c = propA.getPropertyType();
			c = propP.getPropertyType();
			c = propC.getPropertyType();
			c = null;
		}
		System.out.println("testGetPropertyType : " + (System.currentTimeMillis()-start));
	}

	private PropertyDescriptor getPropertyDescriptor(Class clz, String propName) throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(clz);
		PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < props.length; i++) {
			if (propName.equalsIgnoreCase(props[i].getName())) {
				return props[i];
			}
		}
		return null;
	}

	@Ignore("用于测试两种反射的性能")
	@Test
	public void testGetPropertyDescriptors() throws IntrospectionException, ClassNotFoundException {
		Class ownerClass = Class.forName("fd.ng.db.resultset.helper.OneEntity");
		for(int i=0; i<100000; i++) {
			BeanInfo beanInfo = Introspector.getBeanInfo(ownerClass);
			PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		}
	}
}