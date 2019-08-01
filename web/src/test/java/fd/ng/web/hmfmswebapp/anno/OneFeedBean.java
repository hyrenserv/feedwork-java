package fd.ng.web.hmfmswebapp.anno;

import fd.ng.core.bean.FeedBean;

import java.math.BigDecimal;

public class OneFeedBean extends FeedBean {
	private String name;
	private Integer age;
	private BigDecimal money;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = "FeedBean:" + name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}
}
