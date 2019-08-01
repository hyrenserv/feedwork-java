package fd.ng.db.resultset.helper;

import fd.ng.db.entity.anno.Column;

public class TwoEntity extends OneEntity {
	private String myid;
	private int nums;
	private String noReadWrite;
	private String name;
	@Column("class")
	private String uclass;

	public String getMyid() {
		return myid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	public void setMyid(final String myid) {
		this.myid = myid;
	}

	public int getNumber() {
		return nums;
	}

	public void setNumber(final int number) {
		this.nums = number;
	}
}
