package fd.ng.core.utils;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.parser.JSONToken;
import fd.ng.core.utils.beans.OtherClass;
import fd.ng.core.utils.beans.ThreeClass;
import fd.ng.core.utils.beans.json.OneTable;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.lessThan;

public class JsonUtilTest extends FdBaseTestCase {
    @Test
    public void toJson() {
        OtherClass obj = new OtherClass();
        obj.setName("fd");
        obj.setAge(85);
        obj.setMoney(new BigDecimal("150000.99"));
        obj.setAlived(true);
        obj.setCard_id(6225001054267894315L);
        obj.setZip_code("201103");
        ThreeClass threeClass = new ThreeClass();
        threeClass.setName("girl-friend");
        obj.setThreeClass(threeClass);

        String jsonStr = JsonUtil.toJson(obj);
        //System.out.println("json="+jsonStr);
        assertThat(jsonStr, containsString("fd"));
        assertThat(jsonStr, containsString("201103"));
        assertThat(jsonStr, containsString("150000.99"));
        assertThat(jsonStr, containsString("true"));
        assertThat(jsonStr, containsString("6225001054267894315"));
        assertThat(jsonStr, containsString("85"));
        assertThat(jsonStr, containsString("girl-friend"));

        // 测试集合
        OtherClass oc2 = new OtherClass();
        OtherClass oc3 = new OtherClass();
        oc3.setName("xxx");
        List<OtherClass> user = new ArrayList<>();
        user.add(obj);
        user.add(oc2);
        user.add(oc3);

        jsonStr = JsonUtil.toJson(user);
        //System.out.println("json="+jsonStr);
        assertThat(user, hasItem(obj));
        assertThat(user, hasItem(oc2));
        assertThat(user, hasItem(oc3));
    }

    @Ignore
    @Test
    public void toJsonForEntity() throws IllegalAccessException, InstantiationException {
        OneTable oneTable = OneTable.class.newInstance();
        oneTable.setAge(10);
        oneTable.setName("sdf");

        TestCaseLog.println("entity json : " + JsonUtil.toJson(oneTable));

        OneTable oneTable1 = new OneTable();
        oneTable1.setAge(20000);
        oneTable1.setName("20000-sdf");
        TestCaseLog.println("Gson entity json : " + JSON.toJSONString(oneTable1));

        try {
//			Class clazz = ClassUtil.loadClass("fd.ng.core.utils.beans.json.OneTable");
            Class clazz = Class.forName("fd.ng.core.utils.beans.json.OneTable");
            Field field = clazz.getDeclaredField("staticString");
            field.setAccessible(true);
            Set<String> staticString = (Set<String>) field.get(clazz);
            TestCaseLog.println("static field : " + staticString.toString());
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fromJson() {
        String json = "{\"name\":\"fd\",\"age\":85," +
                "\"money\":150000.99,\"alived\":true," +
                "\"zip_code\":\"201103\",\"card_id\":6225001054267894315," +
                "\"threeClass\":{\"name\":\"girl-friend\"}}";
        OtherClass otherClass = JsonUtil.toObject(json, OtherClass.class);

        assertThat(otherClass.getName(), is("fd"));
        assertThat(otherClass.getAge(), is(85));
        assertThat(otherClass.getMoney(), is(new BigDecimal("150000.99")));
        assertThat(otherClass.isAlived(), is(true));
        assertThat(otherClass.getZip_code(), is("201103"));
        assertThat(otherClass.getCard_id(), is(6225001054267894315l));
        assertThat(otherClass.getThreeClass().getName(), is("girl-friend"));
        // 使用allOf和hasProperty方法，将对象作为一个整体，通过一组期望值来匹配。
        assertThat(otherClass, allOf(
                hasProperty("name", is("fd")),
                hasProperty("age", is(85)),
                hasProperty("money", is(new BigDecimal("150000.99"))),
                hasProperty("alived", is(true)),
                hasProperty("zip_code", is("201103")),
                hasProperty("card_id", is(6225001054267894315l)),
                hasProperty("threeClass", notNullValue())
        ));

        // 测试集合的情况
        json = "[{\"name\":\"fd\",\"age\":85,\"money\":150000.99,\"alived\":true," +
                "\"zip_code\":\"201103\",\"card_id\":6225001054267894315," +
                "\"threeClass\":{\"name\":\"girl-friend\"}}," +
                "{\"age\":0,\"alived\":false,\"card_id\":0}," +
                "{\"name\":\"xxx\",\"age\":0,\"alived\":false,\"card_id\":0}]";
        Type type = new TypeReference<List<OtherClass>>() {
        }.getType();
//		Gson m_gson = v2 Gson();
//		List<OtherClass> user = m_gson.toObject(json, type);
        List<OtherClass> user = JsonUtil.toObject(json, type);

        OtherClass oaClass = (OtherClass) user.get(0);
        assertThat(oaClass.getName(), is("fd"));
        assertThat(oaClass.getAge(), is(85));
        assertThat(oaClass.getMoney(), is(new BigDecimal("150000.99")));
        assertThat(oaClass.isAlived(), is(true));
        assertThat(oaClass.getZip_code(), is("201103"));
        assertThat(oaClass.getCard_id(), is(6225001054267894315l));
        assertThat(oaClass.getThreeClass().getName(), is("girl-friend"));
    }

    @Test
    public void getNodeValue() {
        String nameValue = JsonUtil.getNodeValue("{\"name\":\"fd\"}", "name");
        assertThat(nameValue, is("fd"));
        nameValue = JsonUtil.getNodeValue("{\"name\":123}", "name");
        assertThat(nameValue, is("123"));

        nameValue = JsonUtil.getNodeValue("", "name");
        assertThat(nameValue == null, is(true));
        nameValue = JsonUtil.getNodeValue("123", "name");
        assertThat(nameValue == null, is(true));
        // json 数组 - 无头
        String jsonArrayStringNoHead = "[\n" +
                "  { \"name\": \"zhang,san\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "  { \"name\": \"zhang\\\"san\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "  { \"name\": \"lisi\", \"age\": \"20\", \"phone\": \"22222\" }\n" +
                "]\n";
        String valInArrayNoHead = JsonUtil.getNodeValue(jsonArrayStringNoHead, "name");
        assertThat(valInArrayNoHead, is("\"zhang,san\",\"zhang\"\"san\",lisi"));

        // json 数组 - 有头
        String jsonArrayStringHasHead = "{\n" +
                "  \"muser\": [\n" +
                "    { \"name\": \"zhangsan\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "    { \"name\": \"lisi\", \"age\": \"20\", \"phone\": \"22222\" }\n" +
                "  ]\n" +
                "}";
        String valInArrayHasHead = JsonUtil.getNodeValue(jsonArrayStringHasHead, "muser");
        assertThat(valInArrayHasHead,
                is("[{\"name\":\"zhangsan\",\"age\":\"10\",\"phone\":\"11111\"},{\"name\":\"lisi\",\"age\":\"20\",\"phone\":\"22222\"}]"));

        String json_head = "{\"name\":\"fd\",\"age\":85," +
                "\"money\":150000.99,\"alived\":true," +
                "\"zip_code\":\"201103\",\"card_id\":6225001054267894315,";
        String json_three = "\"threeClass\":{\"name\":\"three\", \"age\":\"-10\"}}";
        String json = json_head + json_three;

        String threeClassString = JsonUtil.getNodeValue(json, "threeClass");
        assertThat(threeClassString.length(), lessThan(json.length() - 10));
        assertThat(threeClassString, is("{\"name\":\"three\",\"age\":\"-10\"}"));

        ThreeClass three = JsonUtil.toObject(threeClassString, ThreeClass.class);
        assertThat(three.getName(), is("three"));
        assertThat(three.getAge(), is(-10));
        ThreeClass three1 = JsonUtil.toObjectByNodeName(json, "threeClass", ThreeClass.class);
        assertThat(three1.getName(), is("three"));
        assertThat(three1.getAge(), is(-10));

        Type type = new TypeReference<Map<String, Object>>() {
        }.getType();
        Map<String, Object> threeMap = JsonUtil.toObject(threeClassString, type);
        assertThat(threeMap.get("name"), is("three"));
        assertThat(new Integer(threeMap.get("age").toString()), is(-10));
    }

    // ------------------------  纯测试Gson的使用  ------------------------

    /* 【1】没有数据头的纯数组
[
  { "name": "zhangsan", "age": "10", "phone": "11111" },
  { "name": "lisi", "age": "20", "phone": "22222" }
]
     */
    @Test
    public void testGson_ArrayNoHead() {
		/*Gson gson = new Gson();
		JsonParser parser = new JsonParser(); // JsonParser可以把 JSON 数据分别通过 getAsJsonObject 和 getAsJsonArray 解析成 JsonObject 和 JsonArray
		// JsonElement： 代表 JSON 串中的某一个元素，可以是 JsonObject/JsonArray/JsonPrimitive*/

        String jsonString = "[\n" +
                "  { \"name\": \"zhangsan\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "  { \"name\": \"lisi\", \"age\": \"20\", \"phone\": \"22222\" }\n" +
                "]\n";
        JSONArray jsonArray = JSON.parseArray(jsonString);
        for (Object o : jsonArray) {
            JSONObject json = (JSONObject) JSON.toJSON(o);
            TestCaseLog.println();
            TestCaseLog.println("row RAW : " + json.toString());
            //println("row RAW : " + rowEle.getAsJsonObject().getAsString());

            Map rowMap = JSON.toJavaObject(json, Map.class);
            TestCaseLog.println("row Map : " + rowMap.toString());
        }
    }

    /* 【2】有数据头的纯数组
{
  "muser": [
    { "name": "zhangsan", "age": "10", "phone": "11111" },
    { "name": "lisi", "age": "20", "phone": "22222" }
  ]
}
 */
    @Test
    public void testGson_ArrayHasHead() {
		/*Gson gson = new Gson();
		JsonParser parser = new JsonParser();*/

        String jsonString = "{\n" +
                "  \"muser\": [\n" +
                "    { \"name\": \"zhangsan\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "    { \"name\": \"lisi\", \"age\": \"20\", \"phone\": \"22222\" }\n" +
                "  ]\n" +
                "}";

        // 因为这个 muser 数组是组装在一个 { } 括起来的，也就是说，它在一个Json对象中，所以要先获得这个对象
        JSONObject jsonObject = JSON.parseObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("muser");
        for (Object o : jsonArray) {
            JSONObject json = (JSONObject) JSON.toJSON(o);
            TestCaseLog.println();
            TestCaseLog.println("row RAW : " + json.toString());
            //println("row RAW : " + rowEle.getAsJsonObject().getAsString());
            Map rowMap = JSON.toJavaObject(json, Map.class);
            TestCaseLog.println("row Map : " + rowMap.toString());
        }
    }

    /* 【3】从json串中解析出来任意数据：使用“遍历”的方式，找到预期的数据
{
  "code": 200,
  "msg": "OK",
  "src": [ "java", "c#" ],
  "desc": [ { "s1": "01" }, { "a1": true } ],
  "data": {
    "id" : "A1001",
    "muser" : [
      { "name": "zhangsan", "age": "10", "phone": "11111" },
      { "name": "lisi", "age": "20", "phone": "22222" }
    ]
  },
  "type": ""
}
*/
    @Test
    public void testGson_Anything() {
        String jsonString = "{\n" +
                "  \"code\": 200,\n" +
                "  \"msg\": \"OK\",\n" +
                "  \"src\": [ \"java\", \"c#\" ],\n" +
                "  \"desc\": [ { \"s1\": \"01\" }, { \"a1\": true } ],\n" +
                "  \"data\": {\n" +
                "    \"id\" : \"A1001\",\n" +
                "    \"muser\" : [\n" +
                "      { \"name\": \"zhangsan\", \"age\": \"10\", \"phone\": \"11111\" },\n" +
                "      { \"name\": \"lisi\", \"age\": \"20\", \"phone\": \"22222\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"type\": \"\"\n" +
                "}";

        try (JSONReader reader = new JSONReader(new StringReader(jsonString))) {
            reader.startObject();
            ;//将整个json文件当作 Map<String,Object> 对象来解析 {,}
            while (reader.hasNext()) {
                String tagName = reader.readString();
                //JSONToken tagType = reader.peek();  // 得到当前节点的类型（枚举）
                String tagValue = reader.readString();
                //System.out.println("key=" + tagName + ",val=" + tagValue);
            }
            reader.endObject();
        }
        try (JSONReader jsonReader = new JSONReader(new StringReader(jsonString))) {
            jsonReader.startObject();
            ;//将整个json文件当作 Map<String,Object> 对象来解析 {,}
            while (jsonReader.hasNext()) {
                String key = jsonReader.readString();
                if (key.equals("code")) {
                    Object obj = jsonReader.readObject();//
                    String val = obj.toString();
                    assertThat(key,is("code"));
                    assertThat(val,is("200"));
                }else if(key.equals("msg")){
                    Object obj = jsonReader.readObject();//
                    String val = obj.toString();
                    assertThat(key,is("msg"));
                    assertThat(val,is("OK"));
                }
                else if (key.equals("src")) {
                    jsonReader.startArray();
                    String val = "";
                    while (jsonReader.hasNext()) {
                        String item = jsonReader.readString();
                        val += item+"--";
                    }
                    jsonReader.endArray();
                    assertThat(key,is("src"));
                    assertThat(val,is("java--c#--"));
                }else if(key.equals("desc")) {
                    jsonReader.startArray();//---> [  开启读List对象
                    while(jsonReader.hasNext()) {
                        jsonReader.startObject();
                        while(jsonReader.hasNext()) {
                            String objKey = jsonReader.readString();
                            String objVal = jsonReader.readObject().toString();
                            TestCaseLog.println("objKey: " + objKey + ", objVal: " + objVal);
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();//---> ]
                }else if(key.equals("data")){
                    jsonReader.startObject();
                    while(jsonReader.hasNext()) {
                        String sub_key = jsonReader.readString();
                        Object third_obj = jsonReader.readObject();
                        String subVal = third_obj.toString();//如果需要在读取muser，再while
                        TestCaseLog.println("objKey: " + sub_key + ", objVal: " + subVal);
                    }
                    jsonReader.endObject();
                }else{//通用的写法
                    Object obj = jsonReader.readObject();
                    String val = obj.toString();
                    //System.out.println("objKey: " + key + ", objKey: " + val);
                }
            }
            jsonReader.endObject();
        }

    }

	/*public String getJson(final String jsonString, final String nodeName) {
		try (JSONReader reader = new JSONReader(new StringReader(jsonString))) {
			reader.startObject();
			while (reader.hasNext()) {
				String tagName = reader.readString();
				if (tagName.equals(nodeName)) {
					int tagType = reader.peek(); // 得到当前节点的类型（枚举）
					if(tagType==JSONToken.LITERAL_FLOAT||tagType==JSONToken.LITERAL_INT||tagType==JSONToken.LITERAL_STRING)
						return reader.readObject().toString();
					else if(tagType==JSONToken.NULL)
						return reader.readObject().toString();
					else if(tagType==JSONToken.BEGIN_ARRAY) {
						reader.
						reader.endArray();
					} else if(tagType==JsonToken.BEGIN_OBJECT) {
						reader.beginObject();
						reader.endObject();
					} else {
						throw new RuntimeException("Unsupport JsonType : ( " + tagType + " ) on tagName=" + tagName);
					}
					break;
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}*/

    /**
     * 读一个对象
     *
     * @param reader JsonReader
     */
    private void readJsonObject(JSONReader reader) throws IOException {
        reader.startObject();
        while (reader.hasNext()) {
            String tagName = reader.readString();
            String name = reader.readObject().toString();
        }
        reader.endObject();
    }

    // -------------- 性能测试 ----------------
    @Test
    public void toJsonGson() {
        OtherClass obj = genJavaBean();
        JSON.toJSON(obj);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            obj.setAge(i);
            JSON.toJSON(obj);
        }
        System.out.printf("LoganSquare : %5d, str=%s %n",
                (System.currentTimeMillis() - start), JSON.toJSON(obj));
    }

    @Test
    public void toJsonLoganSquare() throws IOException {
    }

    private OtherClass genJavaBean() {
        OtherClass obj = new OtherClass();
        obj.setName("fd");
        obj.setAge(85);
        obj.setMoney(new BigDecimal("150000.99"));
        obj.setAlived(true);
        obj.setCard_id(6225001054267894315L);
        obj.setZip_code("201103");
        ThreeClass threeClass = new ThreeClass();
        threeClass.setName("girl-friend");
        Map<String, Object> map = new HashMap<>();
        map.put("One", "one");
        map.put("two", 2);
        map.put("three", new String[]{"line1", "line2"});
        threeClass.setFavors(map);
        List<Map<String, Object>> mapList = new ArrayList<>();
        mapList.add(map);
        threeClass.setFriendsFavors(mapList);
        obj.setThreeClass(threeClass);
        return obj;
    }
}
