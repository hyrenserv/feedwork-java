package fd.ng.db.resultset.helper;

import fd.ng.db.entity.anno.Column;

public class TwoBean extends OneBean{
	private String uid;
	@Column("number")
	private int nums;
	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getNums() {
		return nums;
	}

	public void setNums(int nums) {
		this.nums = nums;
	}
}
