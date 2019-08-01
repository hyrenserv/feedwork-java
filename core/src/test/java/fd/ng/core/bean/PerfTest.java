package fd.ng.core.bean;

import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Ignore("测试hash compare equals在使用OftenCache的情况下性能如何")
public class PerfTest {
	@Test
	public void test()
	{
		System.out.println("Hello TestMain! hash compare equals");

		TestObjectWithMultipleFields x = new TestObjectWithMultipleFields(10, 20, 30);
		long start = System.currentTimeMillis();
		final int hashFirst = HashCodeBuilder.reflectionHashCode(x);
		System.out.println("First time : " + (System.currentTimeMillis()-start));

		start = System.currentTimeMillis();
		for(int i=0; i<10_000; i++) {
			final TestObject o1 = new TestObject(i);
			final TestObject o2 = new TestObject(i);

			HashCodeBuilder.reflectionHashCode(x);

			if(HashCodeBuilder.reflectionHashCode(o1) != HashCodeBuilder.reflectionHashCode(o2))
				throw new RuntimeException("hash : o1 != o2");

			if(CompareToBuilder.reflectionCompare(o1, o1)!=0) throw new RuntimeException("CompareTo o1 != o1");
			if(CompareToBuilder.reflectionCompare(o1, o2)!=0) throw new RuntimeException("CompareTo o1 != o2");

			if(!EqualsBuilder.reflectionEquals(o1, o1)) throw new RuntimeException("equals o1 != o1");
			if(!EqualsBuilder.reflectionEquals(o1, o2)) throw new RuntimeException("equals o1 != o2");

			o2.setA(50000000);
			if(CompareToBuilder.reflectionCompare(o1, o2) >= 0) throw new RuntimeException("CompareTo >= 0");
			if(CompareToBuilder.reflectionCompare(o2, o1) <= 0) throw new RuntimeException("CompareTo <= 0");

			if(EqualsBuilder.reflectionEquals(o1, o2)) throw new RuntimeException("equals o1 = o2");
		}
		System.out.println("Often time : " + (System.currentTimeMillis()-start));

		Object[] objs = new Object[]{
				new TestObjectWithMultipleFields(101, 201, 301), new String("sdf"), 1234,
				new HashMap<>(), new HashSet<>(), new ArrayList<>(), new BigDecimal("23"), false,
				"sdffsdff", new ReflectionTestCycleA(), new ReflectionTestCycleB(),
				2344L, new Long("23434"), new Integer("324234"), "lsd3298feklj324", new Object[]{"44jk4jh", 3454, new HashMap<>(10)},
				"ksdkjlsdfjklf", 23094906L, (short)2, 'c',
				"ksdkjls1dfjklf", 1230, "ksdkj2lsdfjklf", 2230, "ksdkjl3sdfjklf", 3230, "ksdk4jlsdfjklf", 4230,
				"ksdkj5ls1dfjklf", 15230, "ksdkj2ls5dfjklf", 25230, "ksdkjl53sdfjklf", 35230, "ksdk4j5lsdfjklf", 45230,
		};
		List<Object> os = new ArrayList<>();
		for(int i=0; i<200; i++) {
			os.add(i);
		}
		for(int i=200; i<400; i++) {
			os.add("sdf"+i);
		}
		for(Object o : objs) os.add(o);
		int size = os.size();
		start = System.currentTimeMillis();
		for(int i=0; i<10_000; i++) {
			final TestObject o1 = new TestObject(i);
			final TestObject o2 = new TestObject(i);

			Object y = os.get(i%size);
			HashCodeBuilder.reflectionHashCode(y);

			if(HashCodeBuilder.reflectionHashCode(o1) != HashCodeBuilder.reflectionHashCode(o2))
				throw new RuntimeException("hash : o1 != o2");

			if(CompareToBuilder.reflectionCompare(o1, o1)!=0) throw new RuntimeException("CompareTo o1 != o1");
			if(CompareToBuilder.reflectionCompare(o1, o2)!=0) throw new RuntimeException("CompareTo o1 != o2");

			if(!EqualsBuilder.reflectionEquals(o1, o1)) throw new RuntimeException("equals o1 != o1");
			if(!EqualsBuilder.reflectionEquals(o1, o2)) throw new RuntimeException("equals o1 != o2");

			o2.setA(50000000);
			if(CompareToBuilder.reflectionCompare(o1, o2) >= 0) throw new RuntimeException("CompareTo >= 0");
			if(CompareToBuilder.reflectionCompare(o2, o1) <= 0) throw new RuntimeException("CompareTo <= 0");

			if(EqualsBuilder.reflectionEquals(o1, o2)) throw new RuntimeException("equals o1 = o2");
		}
		System.out.println("Often time : " + (System.currentTimeMillis()-start));
	}

	static class TestObjectWithMultipleFields {
		@SuppressWarnings("unused")
		private int one = 0;

		@SuppressWarnings("unused")
		private int two = 0;

		@SuppressWarnings("unused")
		private int three = 0;

		TestObjectWithMultipleFields(final int one, final int two, final int three) {
			this.one = one;
			this.two = two;
			this.three = three;
		}
	}

	static class ReflectionTestCycleA {
		ReflectionTestCycleB b;

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	/**
	 * A reflection test fixture.
	 */
	static class ReflectionTestCycleB {
		ReflectionTestCycleA a;

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	static class TestObject implements Comparable<TestObject> {
		private int a;
		TestObject(final int a) {
			this.a = a;
		}
		@Override
		public boolean equals(final Object o) {
			if (o == null) {
				return false;
			}
			if (o == this) {
				return true;
			}
			if (!(o instanceof TestObject)) {
				return false;
			}
			if (o.getClass() != getClass()) {
				return false;
			}
			final TestObject rhs = (TestObject) o;
			return a == rhs.a;
		}

		@Override
		public int hashCode() {
			return a;
		}

		public void setA(final int a) {
			this.a = a;
		}

		public int getA() {
			return a;
		}
		@Override
		public int compareTo(final TestObject rhs) {
			return Integer.compare(a, rhs.a);
		}
	}

	static class TestSubObject extends TestObject {
		private int b;
		TestSubObject() {
			super(0);
		}
		TestSubObject(final int a, final int b) {
			super(a);
			this.b = b;
		}
		@Override
		public boolean equals(final Object o) {
			if (o == null) {
				return false;
			}
			if (o == this) {
				return true;
			}
			if (!(o instanceof TestSubObject)) {
				return false;
			}
			if (o.getClass() != getClass()) {
				return false;
			}
			final TestSubObject rhs = (TestSubObject) o;
			return super.equals(o) && b == rhs.b;
		}
		@Override
		public int hashCode() {
			return b * 17 + super.hashCode();
		}
		public void setB(final int b) {
			this.b = b;
		}

		public int getB() {
			return b;
		}
	}

	static class TestEmptySubObject extends TestObject {
		TestEmptySubObject(final int a) {
			super(a);
		}
	}

	static class TestTransientSubObject extends TestObject {
		@SuppressWarnings("unused")
		private transient int t;
		TestTransientSubObject(final int a, final int t) {
			super(a);
			this.t = t;
		}
	}

	static class TestTSubObject extends TestObject {
		@SuppressWarnings("unused")
		private transient int t;

		TestTSubObject(final int a, final int t) {
			super(a);
			this.t = t;
		}
	}

	static class TestTTSubObject extends TestTSubObject {
		@SuppressWarnings("unused")
		private transient int tt;

		TestTTSubObject(final int a, final int t, final int tt) {
			super(a, t);
			this.tt = tt;
		}
	}

	static class TestTTLeafObject extends TestTTSubObject {
		@SuppressWarnings("unused")
		private final int leafValue;

		TestTTLeafObject(final int a, final int t, final int tt, final int leafValue) {
			super(a, t, tt);
			this.leafValue = leafValue;
		}
	}
}
