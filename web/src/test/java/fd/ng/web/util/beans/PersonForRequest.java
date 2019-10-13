package fd.ng.web.util.beans;

import fd.ng.core.annotation.Param;

import java.math.BigDecimal;

public class PersonForRequest {
	@Param(alias = "username", desc = "d", range = "r")
	String name;
	int age;
	int[] ages;
	String sex;
	String[] favors;
	BigDecimal money;
	@Param(isBean = true, desc = "d", range = "r")
	PersonOtherForRequest otherPerson;

	public PersonOtherForRequest getOtherPerson() {
		return otherPerson;
	}

	public void setOtherPerson(PersonOtherForRequest otherPerson) {
		this.otherPerson = otherPerson;
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

	public int[] getAges() {
		return ages;
	}

	public void setAges(int[] ages) {
		this.ages = ages;
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

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}
//	public void setAmt(String money) {
//		this.money = new BigDecimal(money);
//	}
}
