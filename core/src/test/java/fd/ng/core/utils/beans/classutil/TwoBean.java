package fd.ng.core.utils.beans.classutil;

/**
 * 用于测试获取 Field 和 PropertyDescriptor。
 * 会有 5 个 PropertyDescriptor    ：本类的：name, zip. 父类的：age, onlyRead, onlyWrite
 * 会有 4 个 Field                 ：本类的：name, zip. 父类的：age, xxx
 */
public class TwoBean extends OneBean {
	protected String name;
	private String zip;

	public int getOneProp() { return 1; }
	public void setOneProp(String v) { String k = v; }  // 与 getOneProp 的类型不一致，这个set方法被 PropertyDescriptor 自动忽略

	private Number f1;
	public Integer getF1() { return 1; }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(final String zip) {
		this.zip = zip;
	}
}
