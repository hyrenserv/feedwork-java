package fd.ng.db.conf;

import org.junit.Test;

import javax.sql.DataSource;

public class DbinfosConfTest {
	@Test
	public void read() {
		DataSource db = DbinfosConf.DATA_SOURCE.get("pgsql");
	}
}
