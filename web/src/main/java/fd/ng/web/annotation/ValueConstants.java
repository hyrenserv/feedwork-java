package fd.ng.web.annotation;

/**
 * 常量
 */
public interface ValueConstants {

	/**
	 * 因为不能在注解的属性中使用null来表示空值，所以用这段16位Unicode的字符串来表示空值，
	 * 并且是不会发生这段字符串与用户定义的值是一样的情况。
	 * 避免了用户给出的值却被当作是空值的情况。
	 */
	String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

    Object UNRESOLVED = new Object();
}
