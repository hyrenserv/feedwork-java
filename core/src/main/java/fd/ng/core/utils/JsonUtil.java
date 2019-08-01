package fd.ng.core.utils;

import com.google.gson.*;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.StringJoiner;

/**
 * 大文件用 jackson
 * WEB、微服务等小数据用 GSON
 *
 * Jackson:
 * dependencies {
 * compile group: 'com.fasterxml.jackson.core', name: 'jackson-core', version: jacksonVersion
 * compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: jacksonVersion
 * compile group: 'com.fasterxml.jackson.core', name: 'jackson-annotations', version: jacksonVersion
 * // 引入XML功能
 * compile group: 'com.fasterxml.jackson.dataformat', name: 'jackson-dataformat-xml', version: jacksonVersion
 * // 比JDK自带XML实现更高效的类库
 * compile group: 'com.fasterxml.woodstox', name: 'woodstox-core', version: '5.1.0'
 * // Java 8 新功能
 * compile group: 'com.fasterxml.jackson.datatype', name: 'jackson-datatype-jsr310', version: jacksonVersion
 * compile group: 'com.fasterxml.jackson.module', name: 'jackson-module-parameter-names', version: jacksonVersion
 * compile group: 'com.fasterxml.jackson.datatype', name: 'jackson-datatype-jdk8', version: jacksonVersion
 *
 * compileOnly group: 'org.projectlombok', name: 'lombok', version: '1.16.22'
 * }
 *
 * Gson:
 * compile group: 'com.google.code.gson', name: 'gson', version: '2.8.5'
 */
public class JsonUtil {
	private static final Logger logger = LogManager.getLogger(JsonUtil.class.getName());
	/**
	 * Gson的创建方式二：使用GsonBuilder
	 * 使用new Gson()会创建一个带有默认配置选项的Gson实例，如果不想使用默认配置，那么就可以使用GsonBuilder。
	 * 1) serializeNulls()是GsonBuilder提供的一种配置，当字段值为空或null时，依然对该字段进行转换
	 * Gson gson = v2 GsonBuilder().serializeNulls().create();
	 * 2) excludeFieldsWithoutExposeAnnotation 或 excludeFieldsWithModifiers 排除字段
	 * 3) enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
	 * 4) serializeNulls() //当字段值为空或null时，依然对该字段进行转换
	 * 5) setDateFormat("yyyy-MM-dd HH:mm:ss:SSS") //时间转化为特定格式
	 * 6) setPrettyPrinting() //对结果进行格式化，增加换行
	 * 7) disableHtmlEscaping() //防止特殊字符出现乱码
	 * 8）使用 transient 定义那些不希望被序列化的字段
	 *
	 */
	private static final Gson m_gson = new GsonBuilder()
			.disableHtmlEscaping()
			.setDateFormat("yyyyMMdd HHmmss")
			.setPrettyPrinting()
			.create();
	private static final JsonParser jsonParser = new JsonParser();

	private JsonUtil() { throw new AssertionError("No JsonUtil instances for you!"); }

	/**
	 * 把java对象转成json串。对小数据使用该函数。
	 * 如果要出来大文件，需使用 toJsonOnBigData
	 * @param obj 任意对象
	 * @param <T> 泛型
	 * @return json串
	 */
	public static <T> String toJson(T obj) {
		String jsonStr = m_gson.toJson(obj);
		return jsonStr;
	}

	/**
	 * 把json串转换为java对象。对小数据使用该函数。
	 * 如果要出来大文件，需使用 toJsonOnBigData
	 * @param json
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> T toObject(String json, Class<T> type) {
		T obj = m_gson.fromJson(json, type);
		return obj;
	}

	/**
	 * 对json串里面的一个节点，获取其对象
	 * @param json String 原始json串
	 * @param nodeName String 要获取的节点的名字
	 * @param type Class 只支持主类型和JavaBean
	 * @param <T> 泛型，只支持主类型和JavaBean
	 * @return 获取的对象
	 */
	public static <T> T toObjectByNodeName(String json, String nodeName, Class<T> type) {
		if(json==null||nodeName==null) return null;
		String nodeValue = getNodeValue(json, nodeName);
		return toObject(nodeValue, type);
	}

	/**
	 * 对于复杂类型转换时时候。例如 List<Person> 的复合类型对象。
	 * 第2个参数需要在提前创建：
	 * Type type = new TypeToken<List<Person>>(){}.getType();
	 * @param json
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> T toObject(String json, Type type) {
		T obj = m_gson.fromJson(json, type);
		return obj;
	}

	/**
	 * 根据json中的一个节点的名字，获取其值串。
	 * 如果提取值的节点在数组中，会把所有值拼接成一个CSV格式的串返回。
	 *
	 * @param json json串。如果是""，直接返回null
	 * @param nodeName 需要提取数据的节点名字
	 * @return 返回找到的节点的值串。如果没找到或发生了异常，则返回null。
	 */
	public static String getNodeValue(final String json, final String nodeName) {
		if(json==null||nodeName==null) return null;
		try {
			JsonElement jeRoot = jsonParser.parse(json);
			if (jeRoot.isJsonObject()) {
				return _onlyGetNodeValue(jeRoot, nodeName);
			} else if (jeRoot.isJsonArray()) {
				JsonArray jsonArray = jeRoot.getAsJsonArray();
				StringJoiner result = new StringJoiner(",");
				for (JsonElement rowEle : jsonArray) {
					if(rowEle.isJsonObject()) { // 如果是数组，那么每行必须是一个json对象（即可 {} 包括的串）。因为只有是json对象，才会有节点的名字
						String s = _onlyGetNodeValue(rowEle, nodeName);
						if(s!=null) result.add(CsvUtil.toCsv(s));
					}
				}
				if(result.length()>0) return result.toString();
				else return null;
			} else if (jeRoot.isJsonPrimitive()) {
				logger.debug("origin json string({}) is PrimitiveType Data.", json);
				return null; // 节点是主类型，意味着没有节点名字
			} else if (jeRoot.isJsonNull()) {
				return null;
			} else {
				logger.error("Not json string ! json=[ {} ]", json);
				return null;
			}
		} catch (Exception e) {
			logger.error(json+" nodename="+nodeName, e);
			return null;
		}
	}

	// 根据节点名字，确切的获取他的值，不做递归处理。
	// 传入的 jsonElement 参数，必须是一个json对象，否则会出错
	private static String _onlyGetNodeValue(final JsonElement jsonElement, final String nodeName) {
		JsonElement jeNode = jsonElement.getAsJsonObject().get(nodeName);
		if(jeNode==null) return null; // 该节点不存在
		if (jeNode.isJsonPrimitive())
			return jeNode.getAsJsonPrimitive().getAsString(); // 比如有这个 getAsString ，否则，对于字符串会多出来一对双引号
		else if (jeNode.isJsonObject())
			return jeNode.getAsJsonObject().toString();
		else if (jeNode.isJsonNull()) {
			logger.debug("jsonElement=[ %s ], jsonNode=[ %s ] is JsonNull!", jsonElement, nodeName);
			return StringUtil.EMPTY;
		} else if (jeNode.isJsonArray())
			return jeNode.getAsJsonArray().toString(); // TODO 这里用toString恐怕不行，应该循环处理？
		else {
			throw new RawlayerRuntimeException(String.format("Unsupport json node type! jsonElement=[ %s ], jsonNode=[ %s ]", jsonElement, jeNode));
		}
	}

	/**
	 * 大数据量情况下使用。
	 * 由 Jackson 提供。
	 * @param obj
	 * @param <T>
	 * @return
	 */
	public static <T> String toJsonOnBigData(T obj) {
		return null;
	}

	/**
	 * 大数据量情况下使用
	 * 由 Jackson 提供。
	 * @param json
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> T fromJsonOnBigData(String json, Class<T> type) {
		return null;
	}
}
