package fd.ng.cmdtools.dbeva;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EvaluateMainTest {

	@Test
	public void write() {
		CmdMainImpl main = new CmdMainImpl(new String[]{
				"type=w",
				"total=20", "ban=40",
				"tbl=lsjdlkfjklsdjf"
		});
		main.start();
	}

	@Test
	public void read() {
		CmdMainImpl main = new CmdMainImpl(new String[]{
				"type=r",
				"tbl=increment_hou_water_bak"
		});
		main.start();
	}

	@Test
	public void pagedRead() {
		CmdMainImpl main = new CmdMainImpl(new String[]{
				"type=r",
				"total=20", "pagen=5",
				"tbl=lsjdlkfjklsdjf"
		});
		main.start();
	}

	@Test
	public void clear() {
		CmdMainImpl main = new CmdMainImpl(new String[]{
				"type=d",
				"tbl=lsjdlkfjklsdjf"
		});
		main.start();
	}
}