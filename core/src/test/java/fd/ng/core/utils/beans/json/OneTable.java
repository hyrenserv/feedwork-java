package fd.ng.core.utils.beans.json;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OneTable extends AbsEntity {
	private final String finalString;
	private static final Set<String> staticString;
	private String name;
	private transient int age;
	static {
		Set<String> tmpStaticString = new HashSet<>();
		tmpStaticString.add("name1");
		staticString = Collections.unmodifiableSet(tmpStaticString);
	}
	public OneTable() { addPKName("name"); this.finalString="finalString-value"; }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
