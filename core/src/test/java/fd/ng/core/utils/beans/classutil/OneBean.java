package fd.ng.core.utils.beans.classutil;

import fd.ng.core.annotation.AnnoTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用者： testAccess
 * 用于测试获取 Field 和 PropertyDescriptor。
 * 会有 4 个 PropertyDescriptor    ：name, age, onlyRead, onlyWrite
 * 会有 4 个 Field                 ：name, age, yyy, xxx
 */
public class OneBean {
	protected String name; // 有get/set
	@AnnoTest
	private int age;     // 有get/set
	protected String yyy;  // 有不一样名字的 get/set
	private long xxx;    // 没有 get/set ，所以可以得到这个 Field ，但是无法得到 PropertyDescriptor
	private Map<String, Object> favor = new HashMap<>(); // 没有 get/set， 有add方法，验证 add 是不是可以获得属性

	// 以下两个方法，会导致有两个 PropertyDescriptor ，分别是： onlyRead 和 onlyWrite
	public String getOnlyRead() { return "getPropOnlyRead"; }
	public void setOnlyWrite(int val) { int i = val; }

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getYy() {
		return yyy;
	}

	public void setYy(final String yyy) {
		this.yyy = yyy;
	}

	public int getAge() {
		return age;
	}

	public void setAge(final int age) {
		this.age = age;
	}

	public void addFavor(String name, Object val) {
		favor.put(name, val);
	}
}
