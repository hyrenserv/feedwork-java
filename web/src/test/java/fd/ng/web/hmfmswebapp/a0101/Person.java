package fd.ng.web.hmfmswebapp.a0101;

import fd.ng.core.annotation.Param;

import java.math.BigDecimal;
import java.util.Arrays;

public class Person {
	private String name;
	private int age;
	private String sex;
	private String[] favors;
	@Param(alias = "money",desc = "test", range = "..")
	private BigDecimal amt;
	@Param(isBean = true, desc = "test", range = "..")
	private PersonWomen wife;

	public Person() {}

	public Person(String name, int age, String sex, String[] favors, BigDecimal money) {
		this.name = name;
		this.age = age;
		this.sex = sex;
		this.favors = favors;
		this.amt = money;
		this.wife = null;
	}

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

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String[] getFavors() {
		return favors;
	}

	public void setFavors(String[] favors) {
		this.favors = favors;
	}

	public BigDecimal getAmt() {
		return amt;
	}

	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}

	public PersonWomen getWife() {
		return wife;
	}

	public void setWife(PersonWomen wife) {
		this.wife = wife;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name=p'" + name + '\'' +
				", age=p" + age +
				", sex=p'" + sex + '\'' +
				", favors=p" + Arrays.toString(favors) +
				", amt=p" + amt +
				", wife=" + wife +
				'}';
	}
}
