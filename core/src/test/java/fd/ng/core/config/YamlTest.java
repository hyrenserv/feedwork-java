package fd.ng.core.config;

import fd.ng.core.yaml.YamlArray;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import fd.ng.db.conf.Dbtype;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class YamlTest {
	@Test
	public void read() throws IOException {
//		YamlReader reader = YamlFactory.load("fdconfig/dbinfo.yml");
		YamlMap rootConfig = YamlFactory.getYamlReader(new File("./src/test/resources/fdconfig/dbinfo.yml")).asMap();

		assertThat(rootConfig.value("---NotExist")==null, is(true));

		assertThat(rootConfig.getBool("thisconf.debug.showme"), is(true));

		assertThat(rootConfig.getString("onestring1"), is("sdf lkj"));
		assertThat(rootConfig.getString("onestring2"), is("  sdf # lkj  "));
		assertThat(rootConfig.getString("onestring3"), is(""));
		assertThat(rootConfig.getString("onestring4"), is("src\\test\\main"));
		assertThat(rootConfig.getString("onestring5"), is("55555"));
		assertThat(rootConfig.getString("onestring6"), is("666666"));
		assertThat(rootConfig.getInt("onestring5"), is(55555));
		assertThat(rootConfig.getInt("onestring6"), is(666666));
		assertThat(rootConfig.getLong("onestring5"), is(55555L));
		assertThat(rootConfig.getLong("onestring6"), is(666666L));

		assertThat(rootConfig.getMap("onestring7").getString("xxx"), is("sdfdf 55555"));
		assertThat(rootConfig.getMap("onestring7").getString("yyy"), is("  sdfdf 55555    "));

		assertThat(rootConfig.getArray("onestring8").getString(0), is("one"));
		assertThat(rootConfig.getArray("onestring8").getInt(1), is(2));
		assertThat(rootConfig.getArray("onestring8").getLong(2), is(1111111L));
		assertThat(rootConfig.getArray("onestring8").getBool(3), is(false));
		assertThat(rootConfig.getArray("onestring8").getString(4), is("  # "));

		YamlMap global = rootConfig.getMap("global");
		assertThat(global.getBool("show_sql_time"), is(true));

		YamlArray databases = rootConfig.getArray("databases");
		assertThat(databases.size(), is(2));
		assertThat(databases.getMap(0).getString("name"), is("default"));
		Dbtype dbtype = databases.getMap(0).getEnum(Dbtype.class, "dbtype");
		assertThat(dbtype==Dbtype.MYSQL, is(true));

		YamlMap pMap = rootConfig.getMap("pMap");
		assertThat(pMap.children().size(), is(2));
		YamlMap map1 = pMap.getMap("map1");
		assertThat(map1.getString("xxx"), is("lkjsdf"));
	}
}