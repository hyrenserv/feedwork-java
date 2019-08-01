package fd.ng.web.util.beans;

import java.math.BigDecimal;

// RequestUtilTest 使用的
public class PersonOtherForRequest {
	String oname;
	int oage;
	int[] oages;
	String osex;
	String[] ofavors;
	BigDecimal omoney;

	public String getOname() {
		return oname;
	}

	public void setOname(String oname) {
		this.oname = oname;
	}

	public int getOage() {
		return oage;
	}

	public void setOage(int oage) {
		this.oage = oage;
	}

	public int[] getOages() {
		return oages;
	}

	public void setOages(int[] oages) {
		this.oages = oages;
	}

	public String getOsex() {
		return osex;
	}

	public void setOsex(String osex) {
		this.osex = osex;
	}

	public String[] getOfavors() {
		return ofavors;
	}

	public void setOfavors(String[] ofavors) {
		this.ofavors = ofavors;
	}

	public BigDecimal getOmoney() {
		return omoney;
	}

	public void setOmoney(BigDecimal omoney) {
		this.omoney = omoney;
	}
}
