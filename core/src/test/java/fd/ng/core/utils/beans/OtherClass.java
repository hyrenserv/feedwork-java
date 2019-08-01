package fd.ng.core.utils.beans;

import fd.ng.core.annotation.AnnoTest;

import java.math.BigDecimal;

@AnnoTest
public class OtherClass {
	private String name;
	private int age;
	private BigDecimal money;
	private boolean alived;
	private String zip_code;
	private long card_id;
	private ThreeClass threeClass;

	public ThreeClass getThreeClass() {
		return threeClass;
	}

	public void setThreeClass(ThreeClass threeClass) {
		this.threeClass = threeClass;
	}

	public String getZip_code() {
		return zip_code;
	}

	public void setZip_code(String zip_code) {
		this.zip_code = zip_code;
	}

	public long getCard_id() {
		return card_id;
	}

	public void setCard_id(long card_id) {
		this.card_id = card_id;
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

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public boolean isAlived() {
		return alived;
	}

	public void setAlived(boolean alived) {
		this.alived = alived;
	}
}
