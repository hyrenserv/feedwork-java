package fd.ng.core.cache.bean;

import fd.ng.core.annotation.AnnoTest;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 */
public class ForReadWriteBean {
	private String name;
	@AnnoTest
	private int age;
	private long xxx;  // 没有 get/set ，所以可以得到这个 Field ，但是无法得到 PropertyDescriptor
	private Map<String, Object> map = new HashMap<>();

	// 以下两个方法，会导致有两个 PropertyDescriptor ，分别是： onlyRead 和 onlyWrite
	public String getOnlyRead() { return "getPropOnlyRead"; }
	public void setOnlyWrite(int val) { int i = val; }

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(final int age) {
		this.age = age;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(final Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", "[", "]")
				.add("name='" + name + "'")
				.add("age=" + age)
				.toString();
	}
}
