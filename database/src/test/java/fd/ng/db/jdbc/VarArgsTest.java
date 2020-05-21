package fd.ng.db.jdbc;

import fd.ng.db.conf.Dbtype;
import fd.ng.db.jdbc.nature.PagedSqlInfo;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

@Ignore
public class VarArgsTest {
	@Test
	public void test() {
		method(String.class, "sql1");
		method(String.class, "sql1", 1);
		method("sql2");
		method("sql2",4545);

		hello2("sdf", 3254);
	}

	@Test
	public void ofKeyLableSql() {
		//DatabaseWrapper db = new DatabaseWrapper();
		Dbtype oracle = Dbtype.HIVE;
		Set<String> cc = new HashSet<>();
		cc.add("sdfs");
		cc.add("bb");
		String cccc = oracle.ofKeyLableSql("cccc", cc);
		PagedSqlInfo pagedSqlInfo = oracle.ofPagedSql("select * from aaa", 1, 222);
		String sql = pagedSqlInfo.getSql();
	}

	public <T> Optional<T> method(Class<T> classTypeOfBean, String sql, Object... params) {
		System.out.println("第一个");
		try {
			return Optional.of(classTypeOfBean.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public Map<String, Object> method(String sql, Object... params) {
		System.out.println("第二个");
		return new HashMap<>();
	}

	//带可变参数的方法
	public void hello2(Object ...params) {
		System.out.println("执行带可变参数的方法，参数个数为：" + params.length);
	}
	//带数组参数的方法
	public void hello2(String param, Object ...params) {
		System.out.println("执行带数组参数的方法，数组长度为：" + params);
	}
}
