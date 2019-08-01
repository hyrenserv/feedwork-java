package fd.ng.core.bean;

public interface Builder<T> {
	/**
	 * 返回被构造出来的对象的引用
	 *
	 * @return 构造出来的对象
	 */
	T build();
}
