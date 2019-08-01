package fd.ng.core.yaml;

import java.math.BigDecimal;

/**
 * 这是根据key获取值的唯一操作器
 */
public interface YamlMap extends YamlNode {

	/**
	 * 取得一个key对应的是一个map。
	 * 也就是通过空格缩进后的所有数据数据
	 * @param key key name
	 * @return YamlMap对象。key不存在返回null
	 */
	YamlMap getMap(final String key);
	YamlMap getMap(final YamlNode key);

	/**
	 * 取得一个key对应的是一个数组（yaml sequence）。
	 * 也就是通过空格缩进后的所有数据数据
	 * @param key key name
	 * @return YamlArray对象。key不存在返回null
	 */
	YamlArray getArray(final String key);
	YamlArray getArray(final YamlNode key);

	/**
     * 取得一个key对应的值。
     * @param key key name
     * @return 值按照String类型返回，并且会去掉前后空格，key不存在返回null。
     * 如果希望得到带前后空格、空串等格式的数据，可以使用双引号包裹数据，并且对得到的数据进行相关处理
     */
	String value(final String key);
	String value(final YamlNode key);

	/**
	 * key是否存在
	 * @param key key name
	 * @return 是否存在
	 */
	boolean exist(final String key);

	int getInt(final String key);
	int getInt(final String key, final int defaultValue);
	long getLong(final String key);
	long getLong(final String key, final long defaultValue);
	BigDecimal getDecimal(final String key);
	BigDecimal getDecimal(final String key, final BigDecimal defaultValue);
	boolean getBool(final String key);
	boolean getBool(final String key, final boolean defaultValue);
	String getString(final String key);
	String getString(final String key, final String defaultValue);
	<T extends Enum<T>> T getEnum(Class<T> enumClass, final String key);
	<T extends Enum<T>> T getEnum(Class<T> enumClass, final String key, T defaultValue);
}
