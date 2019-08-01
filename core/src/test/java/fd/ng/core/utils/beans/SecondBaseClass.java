package fd.ng.core.utils.beans;

import fd.ng.core.annotation.AnnoTest;

@AnnoTest
public class SecondBaseClass extends TestBaseClass {
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
