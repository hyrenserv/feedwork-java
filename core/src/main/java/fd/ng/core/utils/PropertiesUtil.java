package fd.ng.core.utils;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 用法：
 * 1） 每个配置文件对应一个工具类
 * 2） 在每个工具类的静态代码段中调用loadProps
 * 3） 使用loadProps获得的Properties，调用各个get方法
 */
public class PropertiesUtil {
	private static final Logger logger = LogManager.getLogger(PropertiesUtil.class);
	public PropertiesUtil() { throw new AssertionError("No PropertiesUtil instances for you!"); }

	/**
	 * 加载属性文件。
	 */
	public static Properties loadProps(String propsPath) {
        if (StringUtil.isEmpty(propsPath)) {
            throw new FrameworkRuntimeException("Parameters cannot be null!");
        }
		try(InputStream is = ClassUtil.getClassLoader("loadProps("+propsPath+")").getResourceAsStream(propsPath)) {
			if (is == null) throw new FrameworkRuntimeException("Properties load failed!");
            Properties props = new Properties();
            props.load(is);
			return props;
		} catch (Exception e) {
			logger.error("load property file["+propsPath+"] failed!", e);
			throw new RawlayerRuntimeException(e);
		}
	}

	/**
	 * 加载属性文件，并转为 Map
	 */
	public static Map<String, String> loadPropsToMap(String propsPath) {
		Map<String, String> map = new HashMap<String, String>();
		Properties props = loadProps(propsPath);
		for (String key : props.stringPropertyNames()) {
			map.put(key, props.getProperty(key));
		}
		return map;
	}

	/**
	 * 获取字符型属性（带有默认值）
	 */
	public static String getString(Properties props, String key, String defaultValue) {
		if (props.containsKey(key)) {
			return props.getProperty(key);
		}
		else
			return defaultValue;
	}

	/**
	 * 获取数值型属性
	 */
	public static int getInt(Properties props, String key, int defaultValue) {
		if (props.containsKey(key)) {
			String val = props.getProperty(key);
			if (StringUtil.isEmpty(val)) {
				return defaultValue;
			}
			else{
				try {
					return Integer.parseInt(val);
				} catch (NumberFormatException e) {
					logger.warn("getInt by key["+key+"] parse Exception!", e);
					return defaultValue;
				}
			}
		}
		else
			return defaultValue;
	}

	/**
	 * 获取布尔型属性
	 */
	public static boolean getBoolean(Properties props, String key, boolean defaultValue) {
		if (props.containsKey(key)) {
			String value = props.getProperty(key);
			return Boolean.parseBoolean(value);
		}
		else
			return defaultValue;
	}

	/**
	 * 获取指定前缀的相关属性
	 */
	public static Map<String, Object> getMap(Properties props, String prefix) {
		Map<String, Object> kvMap = new LinkedHashMap<String, Object>();
		Set<String> keySet = props.stringPropertyNames();
		if (!(keySet==null||keySet.isEmpty())) {
			for (String key : keySet) {
				if (key.startsWith(prefix)) {
					String value = props.getProperty(key);
					kvMap.put(key, value);
				}
			}
		}
		return kvMap;
	}
}
