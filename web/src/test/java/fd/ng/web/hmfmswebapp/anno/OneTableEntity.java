package fd.ng.web.hmfmswebapp.anno;

import fd.ng.db.entity.TableEntity;

import java.math.BigDecimal;

/**
 * 用于测试 request 中传入实体对象
 */
public class OneTableEntity extends TableEntity {
	private String name;
	private Integer age;
	private BigDecimal money;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = "Entity:" + name;
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
