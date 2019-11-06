package fd.ng.web.action;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ActionResult {
	protected Integer code;
	protected String message;
	protected Object data;

	public ActionResult() {
	}

	public ActionResult(Integer code, String message) {
		this(code, message, null);
	}

	public ActionResult(Integer code, String message, Object data) {
		this.code = code;
		this.message = message == null ? StringUtil.EMPTY : message;
		_set_data(data);
	}

	public boolean isSuccess() {
		return (ActionResultEnum.SUCCESS.getCode().equals(code));
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		_set_data(data);
	}

	private void _set_data(Object data) {
		if (data == null)
			this.data = StringUtil.EMPTY;
		else if (data instanceof Result) {
			this.data = ((Result) data).toList();
		} else if (data instanceof Optional) {
			Optional opData = (Optional) data;
			if (opData.isPresent())
				this.data = opData.get();
			else
				this.data = StringUtil.EMPTY;
		} else {
			this.data = data;
		}
	}

	//	/**
//	 * 返回对象。只支持主类型（int/String等），或者是 POJO 类
//	 * 如果包含复杂数据，可使用 getDataObjectByNodeName
//	 * @param clazz
//	 * @param <T>
//	 * @return
//	 */
//	public <T> T getDataObject(Class<T> clazz) {
//		return JsonUtil.toObject(this.data.toString(), clazz);
//	}
//
//	public <T> T getDataObjectByNodeName(String name, Class<T> clazz) {
//		return JsonUtil.toObjectByNodeName(this.data.toString(), name, clazz);
//	}
	@JSONField(serialize = false,deserialize = false)
	public Result getDataForResult() {
		if (data == null) return new Result();
		Type type = new TypeReference<List<Map<String, Object>>>() {
		}.getType();
		List<Map<String, Object>> arData = JsonUtil.toObject(data.toString(), type);
		return new Result(arData);
	}

	@JSONField(serialize = false,deserialize = false)
	public <T> List<T> getDataForEntityList(Class<T> clz) {
		if (data == null) return Collections.emptyList();
		Type type = new TypeReference<List<T>>(clz) {
		}.getType();
		return JsonUtil.toObject(data.toString(), type);
	}

	@JSONField(serialize = false,deserialize = false)
	public <K,V> Map<K,V> getDataForMap() {
		if (data == null) return Collections.emptyMap();
		Type type = new TypeReference<Map<K,V>>() {
		}.getType();
		return JsonUtil.toObject(data.toString(), type);
	}

	@JSONField(serialize = false,deserialize = false)
	public <K,V> Map<K,V> getDataForMap(Class<K> kClass,Class<V> vClass) {
		if (data == null) return Collections.emptyMap();
		Type type = new TypeReference<Map<K,V>>(kClass,vClass) {
		}.getType();
		return JsonUtil.toObject(data.toString(), type);
	}

	@Override
	public String toString() {
		return "ActionResult{" +
				"code=" + code +
				", message='" + message + '\'' +
				", data=" + data +
				'}';
	}
}
