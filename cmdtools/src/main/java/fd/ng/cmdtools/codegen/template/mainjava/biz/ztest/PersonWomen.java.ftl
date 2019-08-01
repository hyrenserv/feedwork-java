package ${basePackage}.${subPackage};

import fd.ng.web.annotation.RequestParam;

import java.util.StringJoiner;

public class PersonWomen {
	@RequestParam(name = "name")
	private String husbandName;
	private int age;
	private String sex;

	public String getHusbandName() {
		return husbandName;
	}

	public void setHusbandName(String husbandName) {
		this.husbandName = husbandName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", PersonWomen.class.getSimpleName() + "[", "]")
				.add("husbandName=w'" + husbandName + "'")
				.add("age=w" + age)
				.add("sex=w'" + sex + "'")
				.toString();
	}
}
