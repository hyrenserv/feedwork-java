package fd.ng.core.exception.beans;

import java.sql.SQLException;

public class OneClass {
	public void compute() {
		int i = 1/0;
	}

	public void execSQL() throws SQLException {
		try {
			String a = null;
			a.substring(0, 1);
		} catch (Exception e) {
			//e.printStackTrace();
			throw new SQLException("test SQLException from func", e);
		}
	}
}
