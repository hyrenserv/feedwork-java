package fd.ng.web.action.actioninstancehelper;

import fd.ng.core.utils.ClassUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.helper.ActionInstanceHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 做这个测试，需要把 appinfo.conf 中的 base.package 修改为: fd.ng.web.action.actioninstancehelper
 * 因此，这个测试类只能单独使用，不能进入集成测试中
 */
@Ignore("需修改appinfo.conf后手工执行")
public class ActionInstanceHelperTest {
	@BeforeClass
	public static void start() throws ClassNotFoundException {
		System.out.println("本测试用例的使用方式：");
		System.out.println("把 appinfo.conf 中的 base.package 修改为: fd.ng.web.action.actioninstancehelper");

		System.out.println("Current class loader : " + Thread.currentThread().getContextClassLoader());
		ClassUtil.loadClass(WebinfoConf.class.getName());
		ClassUtil.loadClass(DbinfosConf.class.getName());
		ClassUtil.loadClass(ActionInstanceHelper.class.getName());
	}

	/**
	 * 这个方法内部逻辑没有意义，仅仅是为了激活测试 loadClass ActionInstanceHelper
	 */
	@Test
	public void getMethod() {
		Stream<String> names = Stream.of("Lamurudu", "Okanbi", "Oduduwa");
		Optional<String> longest = names
				.filter(name -> name.startsWith("L"))
				.findFirst();
		longest.ifPresent(name -> {
			String s = name.toUpperCase();
			System.out.println("The longest name is "+ s);
		});
	}
}
