package fd.ng.core.utils.beans;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ThreeClass extends SecondBaseClass {
	protected Map<String, Object> favors;
	protected int age;
	protected BigDecimal money;
	protected List<Map<String, Object>> friendsFavors;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getFavors() {
		return favors;
	}

	public void setFavors(Map<String, Object> favors) {
		this.favors = favors;
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

	public List<Map<String, Object>> getFriendsFavors() {
		return friendsFavors;
	}

	public void setFriendsFavors(List<Map<String, Object>> friendsFavors) {
		this.friendsFavors = friendsFavors;
	}
}
