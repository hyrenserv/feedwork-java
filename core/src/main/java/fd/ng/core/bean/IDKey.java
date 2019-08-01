package fd.ng.core.bean;

final class IDKey {
	private final Object value;
	private final int id;

	/**
	 * Constructor for IDKey
	 *
	 * @param _value The value
	 */
	IDKey(final Object _value) {
		// 系统级的hashcode，不同于Object的hashcode，identityHashCode是根据对象在内存中的地址算出来的一个数值。
		// 不同的地址算出来的结果是不一样的，例如：
		// String A = new String("");    String B = new String("");
		// 两个对象的hashCode()函数返回值一样，但是，被identityHashCode函数返回的值不一样
		id = System.identityHashCode(_value);
		// (LANG-459) 有些情况下，不同的对象会得到相同的hashcode
		// 因此，value 也可以用来消除这些歧义
		value = _value;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof IDKey)) {
			return false;
		}
		final IDKey idKey = (IDKey) other;
		if (id != idKey.id) {
			return false;
		}
		return value == idKey.value;
	}
}
