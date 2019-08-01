package fd.ng.core.utils;

import fd.ng.core.bean.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

public class ArrayUtil {
	/**
	 * An empty immutable {@code Object} array.
	 */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	/**
	 * An empty immutable {@code Class} array.
	 */
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
	/**
	 * An empty immutable {@code String} array.
	 */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	/**
	 * An empty immutable {@code long} array.
	 */
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	/**
	 * An empty immutable {@code Long} array.
	 */
	public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
	/**
	 * An empty immutable {@code int} array.
	 */
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	/**
	 * An empty immutable {@code Integer} array.
	 */
	public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
	/**
	 * An empty immutable {@code short} array.
	 */
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	/**
	 * An empty immutable {@code Short} array.
	 */
	public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
	/**
	 * An empty immutable {@code byte} array.
	 */
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	/**
	 * An empty immutable {@code Byte} array.
	 */
	public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
	/**
	 * An empty immutable {@code boolean} array.
	 */
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	/**
	 * An empty immutable {@code Boolean} array.
	 */
	public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
	/**
	 * An empty immutable {@code char} array.
	 */
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];
	/**
	 * An empty immutable {@code Character} array.
	 */
	public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

	/**
	 * The index value when an element is not found in a list or array: {@code -1}.
	 * This value is returned by methods in this class and can also be used in comparisons with values returned by
	 * various method from {@link java.util.List}.
	 */
	public static final int INDEX_NOT_FOUND = -1;

	public static boolean isEmpty(Object[] v) {
		if(v==null||v.length==0) return true;
		else return false;
	}
	public static boolean isNotEmpty(Object[] v) {
		if(v==null||v.length==0) return false;
		else return true;
	}

	public static boolean contains(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
	}
	public static int indexOf(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}
	public static int indexOf(final long[] array, final long valueToFind) {
		return indexOf(array, valueToFind, 0);
	}
	public static int indexOf(final long[] array, final long valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}
	public static int indexOf(final int[] array, final int valueToFind) {
		return indexOf(array, valueToFind, 0);
	}
	public static int indexOf(final int[] array, final int valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}
	public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = startIndex; i < array.length; i++) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static Map<Object, Object> toMap(final Object[] array) {
		if (array == null) {
			return null;
		}
		final Map<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
		for (int i = 0; i < array.length; i++) {
			final Object object = array[i];
			if (object instanceof Map.Entry<?, ?>) {
				final Map.Entry<?,?> entry = (Map.Entry<?,?>) object;
				map.put(entry.getKey(), entry.getValue());
			} else if (object instanceof Object[]) {
				final Object[] entry = (Object[]) object;
				if (entry.length < 2) {
					throw new IllegalArgumentException("Array element " + i + ", '"
							+ object
							+ "' length must be greater than 1");
				}
				map.put(entry[0], entry[1]);
			} else {
				throw new IllegalArgumentException("Array element " + i + ", '"
						+ object
						+ "', is neither of type Map.Entry nor an Array");
			}
		}
		return map;
	}

	public static int hashCode(final Object array) {
		return new HashCodeBuilder().append(array).toHashCode();
	}

	public ArrayUtil() { throw new AssertionError("No ArrayUtil instances for you!"); }
}
