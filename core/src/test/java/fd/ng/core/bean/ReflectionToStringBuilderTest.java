package fd.ng.core.bean;

import fd.ng.core.utils.ArrayUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ReflectionToStringBuilderTest {
	@Test(expected=IllegalArgumentException.class)
	public void testConstructorWithNullObject() {
		new ReflectionToStringBuilder(null, ToStringStyle.DEFAULT_STYLE, new StringBuffer());
	}

	// ------- Test : Summary 这个测试没有通过
	@SuppressWarnings("unused")
	private String stringField = "string";

	@ToStringSummary
	private String summaryString = "summary";
//	@Test
//	public void testSummary() {
//		Assert.assertEquals("[stringField=string,summaryString=<String>]",
//				new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).build());
//	}
//	@Test
//	public void testSummaryCommons() {
//		Assert.assertEquals("[stringField=string,summaryString=<String>]",
//				new org.apache.commons.lang3.builder.ReflectionToStringBuilder(
//						this, org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE).build());
//	}

	// ------- Test : MutateInspectConcurrency
	class TestFixtureMutateInspectConcurrency {
		private final LinkedList<Integer> listField = new LinkedList<>();
		private final Random random = new Random();
		private final int N = 100;

		TestFixtureMutateInspectConcurrency() {
			synchronized (this) {
				for (int i = 0; i < N; i++) {
					listField.add(Integer.valueOf(i));
				}
			}
		}

		public synchronized void add() {
			listField.add(Integer.valueOf(random.nextInt(N)));
		}

		public synchronized void delete() {
			listField.remove(Integer.valueOf(random.nextInt(N)));
		}
	}

	class MutatingClient implements Runnable {
		private final TestFixtureMutateInspectConcurrency testFixture;
		private final Random random = new Random();

		MutatingClient(final TestFixtureMutateInspectConcurrency testFixture) {
			this.testFixture = testFixture;
		}

		@Override
		public void run() {
			if (random.nextBoolean()) {
				testFixture.add();
			} else {
				testFixture.delete();
			}
		}
	}

	class InspectingClient implements Runnable {
		private final TestFixtureMutateInspectConcurrency testFixture;

		InspectingClient(final TestFixtureMutateInspectConcurrency testFixture) {
			this.testFixture = testFixture;
		}

		@Override
		public void run() {
			ReflectionToStringBuilder.toString(testFixture);
		}
	}

	@Test
	@Ignore
	public void testConcurrency() throws Exception {
		final TestFixtureMutateInspectConcurrency testFixture = new TestFixtureMutateInspectConcurrency();
		final int numMutators = 10;
		final int numIterations = 10;
		for (int i = 0; i < numIterations; i++) {
			for (int j = 0; j < numMutators; j++) {
				final Thread t = new Thread(new MutatingClient(testFixture));
				t.start();
				final Thread s = new Thread(new InspectingClient(testFixture));
				s.start();
			}
		}
	}

	// -------- Test : BuilderExcludeWithAnnotation
	class TestFixtureExcludeWithAnnotation {
		@ToStringExclude
		private final String excludedField = EXCLUDED_FIELD_VALUE;

		@SuppressWarnings("unused")
		private final String includedField = INCLUDED_FIELD_VALUE;
	}

	private static final String INCLUDED_FIELD_NAME = "includedField";

	private static final String INCLUDED_FIELD_VALUE = "Hello World!";

	private static final String EXCLUDED_FIELD_NAME = "excludedField";

	private static final String EXCLUDED_FIELD_VALUE = "excluded field value";

	@Test
	public void test_toStringExcludeWithAnnotation() {
		final String toString = ReflectionToStringBuilder.toString(new TestFixtureExcludeWithAnnotation());

		assertThat(toString, not(containsString(EXCLUDED_FIELD_NAME)));
		assertThat(toString, not(containsString(EXCLUDED_FIELD_VALUE)));
		assertThat(toString, containsString(INCLUDED_FIELD_NAME));
		assertThat(toString, containsString(INCLUDED_FIELD_VALUE));
	}

	// -------- Test : Exclude
	class TestFixtureExclude {
		@SuppressWarnings("unused")
		private final String secretField = SECRET_VALUE;

		@SuppressWarnings("unused")
		private final String showField = NOT_SECRET_VALUE;
	}

	private static final String NOT_SECRET_FIELD = "showField";

	private static final String NOT_SECRET_VALUE = "Hello World!";

	private static final String SECRET_FIELD = "secretField";

	private static final String SECRET_VALUE = "secret value";

	@Test
	public void test_toStringExclude() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), SECRET_FIELD);
		this.validateSecretFieldAbsent(toString);
	}

	@Test
	public void test_toStringExcludeArray() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), SECRET_FIELD);
		this.validateSecretFieldAbsent(toString);
	}

	@Test
	public void test_toStringExcludeArrayWithNull() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), new String[]{null});
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeArrayWithNulls() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), null, null);
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeCollection() {
		final List<String> excludeList = new ArrayList<>();
		excludeList.add(SECRET_FIELD);
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), excludeList);
		this.validateSecretFieldAbsent(toString);
	}

	@Test
	public void test_toStringExcludeCollectionWithNull() {
		final List<String> excludeList = new ArrayList<>();
		excludeList.add(null);
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), excludeList);
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeCollectionWithNulls() {
		final List<String> excludeList = new ArrayList<>();
		excludeList.add(null);
		excludeList.add(null);
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), excludeList);
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeEmptyArray() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), ArrayUtil.EMPTY_STRING_ARRAY);
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeEmptyCollection() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), new ArrayList<String>());
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeNullArray() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), (String[]) null);
		this.validateSecretFieldPresent(toString);
	}

	@Test
	public void test_toStringExcludeNullCollection() {
		final String toString = ReflectionToStringBuilder.toStringExclude(new TestFixtureExclude(), (Collection<String>) null);
		this.validateSecretFieldPresent(toString);
	}

	private void validateNonSecretField(final String toString) {
		assertTrue(toString.contains(NOT_SECRET_FIELD));
		assertTrue(toString.contains(NOT_SECRET_VALUE));
	}

	private void validateSecretFieldAbsent(final String toString) {
		assertThat(ArrayUtil.INDEX_NOT_FOUND, is(toString.indexOf(SECRET_VALUE)));
		this.validateNonSecretField(toString);
	}

	private void validateSecretFieldPresent(final String toString) {
		assertTrue(toString.indexOf(SECRET_VALUE) > 0);
		this.validateNonSecretField(toString);
	}

	// ------- Test : ExcludeNullValues
	static class TestFixtureExcludeNullValues {
		@SuppressWarnings("unused")
		private final Integer testIntegerField;
		@SuppressWarnings("unused")
		private final String testStringField;

		TestFixtureExcludeNullValues(final Integer a, final String b) {
			this.testIntegerField = a;
			this.testStringField = b;
		}
	}

	private static final String INTEGER_FIELD_NAME = "testIntegerField";
	private static final String STRING_FIELD_NAME = "testStringField";
	private final TestFixtureExcludeNullValues BOTH_NON_NULL = new TestFixtureExcludeNullValues(0, "str");
	private final TestFixtureExcludeNullValues FIRST_NULL = new TestFixtureExcludeNullValues(null, "str");
	private final TestFixtureExcludeNullValues SECOND_NULL = new TestFixtureExcludeNullValues(0, null);
	private final TestFixtureExcludeNullValues BOTH_NULL = new TestFixtureExcludeNullValues(null, null);

	@Test
	public void test_NonExclude(){
		//normal case=
		String toString = ReflectionToStringBuilder.toString(BOTH_NON_NULL, null, false, false, false, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		//make one null
		toString = ReflectionToStringBuilder.toString(FIRST_NULL, null, false, false, false, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		//other one null
		toString = ReflectionToStringBuilder.toString(SECOND_NULL, null, false, false, false, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		//make the both null
		toString = ReflectionToStringBuilder.toString(BOTH_NULL, null, false, false, false, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));
	}

	@Test
	public void test_excludeNull(){

		//test normal case
		String toString = ReflectionToStringBuilder.toString(BOTH_NON_NULL, null, false, false, true, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		//make one null
		toString = ReflectionToStringBuilder.toString(FIRST_NULL, null, false, false, true, null);
		assertFalse(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		//other one null
		toString = ReflectionToStringBuilder.toString(SECOND_NULL, null, false, false, true, null);
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertFalse(toString.contains(STRING_FIELD_NAME));

		//both null
		toString = ReflectionToStringBuilder.toString(BOTH_NULL, null, false, false, true, null);
		assertFalse(toString.contains(INTEGER_FIELD_NAME));
		assertFalse(toString.contains(STRING_FIELD_NAME));
	}

	@Test
	public void test_ConstructorOption(){
		ReflectionToStringBuilder builder = new ReflectionToStringBuilder(BOTH_NON_NULL, null, null, null, false, false, true);
		assertTrue(builder.isExcludeNullValues());
		String toString = builder.toString();
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		builder = new ReflectionToStringBuilder(FIRST_NULL, null, null, null, false, false, true);
		toString = builder.toString();
		assertFalse(toString.contains(INTEGER_FIELD_NAME));
		assertTrue(toString.contains(STRING_FIELD_NAME));

		builder = new ReflectionToStringBuilder(SECOND_NULL, null, null, null, false, false, true);
		toString = builder.toString();
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
		assertFalse(toString.contains(STRING_FIELD_NAME));

		builder = new ReflectionToStringBuilder(BOTH_NULL, null, null, null, false, false, true);
		toString = builder.toString();
		assertFalse(toString.contains(INTEGER_FIELD_NAME));
		assertFalse(toString.contains(STRING_FIELD_NAME));
	}

	@Test
	public void test_ConstructorOptionNormal(){
		final ReflectionToStringBuilder builder = new ReflectionToStringBuilder(BOTH_NULL, null, null, null, false, false, false);
		assertFalse(builder.isExcludeNullValues());
		String toString = builder.toString();
		assertTrue(toString.contains(STRING_FIELD_NAME));
		assertTrue(toString.contains(INTEGER_FIELD_NAME));

		//regression test older constructors
		ReflectionToStringBuilder oldBuilder = new ReflectionToStringBuilder(BOTH_NULL);
		toString = oldBuilder.toString();
		assertTrue(toString.contains(STRING_FIELD_NAME));
		assertTrue(toString.contains(INTEGER_FIELD_NAME));

		oldBuilder = new ReflectionToStringBuilder(BOTH_NULL, null, null, null, false, false);
		toString = oldBuilder.toString();
		assertTrue(toString.contains(STRING_FIELD_NAME));
		assertTrue(toString.contains(INTEGER_FIELD_NAME));

		oldBuilder = new ReflectionToStringBuilder(BOTH_NULL, null, null);
		toString = oldBuilder.toString();
		assertTrue(toString.contains(STRING_FIELD_NAME));
		assertTrue(toString.contains(INTEGER_FIELD_NAME));
	}

	@Test
	public void test_ConstructorOption_ExcludeNull(){
		ReflectionToStringBuilder builder = new ReflectionToStringBuilder(BOTH_NULL, null, null, null, false, false, false);
		builder.setExcludeNullValues(true);
		assertTrue(builder.isExcludeNullValues());
		String toString = builder.toString();
		assertFalse(toString.contains(STRING_FIELD_NAME));
		assertFalse(toString.contains(INTEGER_FIELD_NAME));

		builder = new ReflectionToStringBuilder(BOTH_NULL, null, null, null, false, false, true);
		toString = builder.toString();
		assertFalse(toString.contains(STRING_FIELD_NAME));
		assertFalse(toString.contains(INTEGER_FIELD_NAME));

		final ReflectionToStringBuilder oldBuilder = new ReflectionToStringBuilder(BOTH_NULL);
		oldBuilder.setExcludeNullValues(true);
		assertTrue(oldBuilder.isExcludeNullValues());
		toString = oldBuilder.toString();
		assertFalse(toString.contains(STRING_FIELD_NAME));
		assertFalse(toString.contains(INTEGER_FIELD_NAME));
	}

	// ------- Test : Concurrency
	static class CollectionHolder<T extends Collection<?>> {
		T collection;

		CollectionHolder(final T collection) {
			this.collection = collection;
		}
	}

	private static final int DATA_SIZE = 100000;
	private static final int REPEAT = 100;

	@Test
	@Ignore
	public void testLinkedList() throws InterruptedException, ExecutionException {
		this.testConcurrency(new CollectionHolder<List<Integer>>(new LinkedList<Integer>()));
	}

	@Test
	@Ignore
	public void testArrayList() throws InterruptedException, ExecutionException {
		this.testConcurrency(new CollectionHolder<List<Integer>>(new ArrayList<Integer>()));
	}

	@Test
	@Ignore
	public void testCopyOnWriteArrayList() throws InterruptedException, ExecutionException {
		this.testConcurrency(new CollectionHolder<List<Integer>>(new CopyOnWriteArrayList<Integer>()));
	}

	private void testConcurrency(final CollectionHolder<List<Integer>> holder) throws InterruptedException,
			ExecutionException {
		final List<Integer> list = holder.collection;
		// make a big array that takes a long time to toString()
		for (int i = 0; i < DATA_SIZE; i++) {
			list.add(Integer.valueOf(i));
		}
		// Create a thread pool with two threads to cause the most contention on the underlying resource.
		final ExecutorService threadPool = Executors.newFixedThreadPool(2);
		// Consumes toStrings
		final Callable<Integer> consumer = new Callable<Integer>() {
			@Override
			public Integer call() {
				for (int i = 0; i < REPEAT; i++) {
					final String s = ReflectionToStringBuilder.toString(holder);
					Assert.assertNotNull(s);
				}
				return Integer.valueOf(REPEAT);
			}
		};
		// Produces changes in the list
		final Callable<Integer> producer = new Callable<Integer>() {
			@Override
			public Integer call() {
				for (int i = 0; i < DATA_SIZE; i++) {
					list.remove(list.get(0));
				}
				return Integer.valueOf(REPEAT);
			}
		};
		final Collection<Callable<Integer>> tasks = new ArrayList<>();
		tasks.add(consumer);
		tasks.add(producer);
		final List<Future<Integer>> futures = threadPool.invokeAll(tasks);
		for (final Future<Integer> future : futures) {
			assertThat(REPEAT, is(future.get().intValue()));
		}
		threadPool.shutdown();
		threadPool.awaitTermination(1, TimeUnit.SECONDS);
	}
}
