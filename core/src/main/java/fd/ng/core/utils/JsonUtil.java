package fd.ng.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * 大文件用 jackson
 * WEB、微服务等小数据用 GSON
 * <p>
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
 * <p>
 * compileOnly group: 'org.projectlombok', name: 'lombok', version: '1.16.22'
 * }
 * <p>
 * Gson:
 * compile group: 'com.google.code.gson', name: 'gson', version: '2.8.5'
 */
public class JsonUtil {
    private static final Logger logger = LogManager.getLogger(JsonUtil.class.getName());

    private JsonUtil() {
        throw new AssertionError("No JsonUtil instances for you!");
    }

    /**
     * 把java对象转成json串。对小数据使用该函数。
     * 如果要出来大文件，需使用 toJsonOnBigData
     *
     * @param obj 任意对象
     * @param <T> 泛型
     * @return json串
     */
    public static <T> String toJson(T obj) {
        String jsonStr = JSON.toJSONString(obj);
        return jsonStr;
    }

    /**
     * 把json串转换为java对象。对小数据使用该函数。
     * 如果要出来大文件，需使用 toJsonOnBigData
     *
     * @param json String 原始json串
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T toObject(String json, Class<T> type) {
        return toObjectSafety(json,type).orElse(null);
    }

    /**
     * 把json串转换为java对象。对小数据使用该函数。
     * 如果要出来大文件，需使用 toJsonOnBigData
     * 在action中强烈建议使用该方法
     * @param json  String 原始json串
     * @param type
     * @param <T>
     * @return Optional 对象
     */
    public static <T> Optional<T> toObjectSafety(String json, Class<T> type) {
        try {
            return Optional.of(JSON.parseObject(json,type));
        }catch (Exception e){
            return Optional.empty();
        }
    }
    /**
     * 对json串里面的一个节点，获取其对象
     *
     * @param json     String 原始json串
     * @param nodeName String 要获取的节点的名字
     * @param type     Class 只支持主类型和JavaBean
     * @param <T>      泛型，只支持主类型和JavaBean
     * @return 获取的对象
     */
    public static <T> T toObjectByNodeName(String json, String nodeName, Class<T> type) {
        return toObjectByNodeNameSafety(json,nodeName,type).orElse(null);
    }

    /**
     * 对json串里面的一个节点，获取其对象
     * 在action中强烈建议使用该方法
     * @param json     String 原始json串
     * @param nodeName String 要获取的节点的名字
     * @param type     Class 只支持主类型和JavaBean
     * @param <T>      泛型，只支持主类型和JavaBean
     * @return Optional 获取的对象
     */
    public static <T> Optional<T> toObjectByNodeNameSafety(String json, String nodeName, Class<T> type) {
        if (json == null || nodeName == null) return null;
        String nodeValue = getNodeValue(json, nodeName);
        return toObjectSafety(nodeValue, type);
    }
    /**
     * 对于复杂类型转换时时候。例如 List<Person> 的复合类型对象。
     * 第2个参数需要在提前创建：
     * Type type = new TypeReference<List<Person>>(){}.getType();
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */


    public static <T> T toObject(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    /**
     * 根据json中的一个节点的名字，获取其值串。
     * 如果提取值的节点在数组中，会把所有值拼接成一个CSV格式的串返回。
     *
     * @param json     json串。如果是""，直接返回null
     * @param nodeName 需要提取数据的节点名字
     * @return 返回找到的节点的值串。如果没找到或发生了异常，则返回null。
     */
    public static String getNodeValue(final String json, final String nodeName) {
        if (json == null || nodeName == null) return null;
        try {
            Object jeRoot = JSON.parse(json, Feature.OrderedField);
            if (jeRoot instanceof JSONObject) {
                return _onlyGetNodeValue((JSONObject) jeRoot, nodeName);
            } else if (jeRoot instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jeRoot;
                StringJoiner result = new StringJoiner(",");
                for (Object rowEle : jsonArray) {
                    if (rowEle instanceof JSONObject) { // 如果是数组，那么每行必须是一个json对象（即可 {} 包括的串）。因为只有是json对象，才会有节点的名字
                        String s = _onlyGetNodeValue((JSONObject) rowEle, nodeName);
                        if (s != null) result.add(CsvUtil.toCsv(s));
                    }
                }
                if (result.length() > 0) return result.toString();
                else return null;
            } else {
                logger.error("Not json string ! json=[ {} ]", json);
                return null;
            }
        } catch (Exception e) {
            logger.error(json + " nodename=" + nodeName, e);
            return null;
        }
    }

    // 根据节点名字，确切的获取他的值，不做递归处理。
    // 传入的 jsonElement 参数，必须是一个json对象，否则会出错
    private static String _onlyGetNodeValue(final JSONObject jsonObject, final String nodeName) {
        Object jeNode = jsonObject.get(nodeName);
        if (!jsonObject.containsKey(nodeName)) return null; // 该节点不存在
        if (jeNode == null) {
            logger.debug("jsonElement=[ %s ], jsonNode=[ %s ] is JsonNull!", jsonObject, nodeName);
            return StringUtil.EMPTY;
        }
        return jeNode.toString();
    }


    /**
     * 大数据量情况下使用。
     * 由 Jackson 提供。
     *
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
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJsonOnBigData(String json, Class<T> type) {
        return null;
    }
}
