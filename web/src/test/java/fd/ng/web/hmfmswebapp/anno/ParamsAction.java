package fd.ng.web.hmfmswebapp.anno;

import fd.ng.web.annotation.Params;
import fd.ng.web.hmfmswebapp.a0101.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 服务于 Params 注解测试的 Action。
 * 由于 Params 注解不建议使用，所以本测试类不需要执行。
 * 另外，改来改去的，这个类及其对应的测试类，已经很久没执行过了，可以已经无法运行了。
 * 真要使用 Params 注解前，改改这个类，做好充足测试再用。
 * 另外记得，如果要用 Params 注解，需把 WebServlet 中的 autowireParameters 更换为 assignParameters
 */
@Deprecated
public class ParamsAction //extends WebappBaseAction
{
	private static final Logger logger = LogManager.getLogger(ParamsAction.class.getName());
	public String index() {
		return "index";
	}

	// 参数：money，不应该被赋值，即使POST过来的数据中存在money
	@Params("String username, HttpServletRequest request, int age")
	public String testParamsRequest(String name, HttpServletRequest request, int age, BigDecimal money) {
		return ("name=" + name + ", age=" + age + ", arg_money=" + money
				+ ", req_money=" + request.getParameter("money")
		);
	}

	// 参数：money，不应该被赋值，即使POST过来的数据中存在money
	@Params("String name, HttpServletRequest request, int age, Person person")
	public String testParamsPerson(String name, HttpServletRequest request, int age, Person person, BigDecimal money) {
		return ("name=" + name + ", age=" + age + ", arg_money=" + money
				+ ", req_money=" + request.getParameter("money")
				+ ", person=" + person
		);
	}

	@Params("String username, String sex:null, String[] addr, int age, int[] ages")
	public String testParamsString_int(String name, String sex, String[] addr, int age, int[] ages) {
		return ("name=" + name + ", sex=" + sex + ", addr=" + Arrays.toString(addr)
				+ ", age=" + age + ", ages=" + Arrays.toString(ages)
		);
	}
	@Params("String username, String sex:null, Integer age, Integer noage:null, Integer[] ages")
	public String testParamsInteger(String name, String sex, Integer age, Integer noage, Integer[] ages) {
		return ("name=" + name + ", sex=" + sex
				+ ", age=" + age + ", noage=" + noage + ", ages=" + Arrays.toString(ages)
		);
	}
	@Params("String username, long age, long[] ages")
	public String testParams_long(String name, long age, long[] ages) {
		return ("name=" + name
				+ ", age=" + age + ", ages=" + Arrays.toString(ages)
		);
	}
	@Params("String username, Long age, Long noage:null, Long[] ages:null")
	public String testParamsLong(String name, Long age, Long noage, Long[] ages) {
		return ("name=" + name
				+ ", age=" + age + ", noage=" + noage + ", ages=" + Arrays.toString(ages)
		);
	}
	@Params("String username, BigDecimal money, BigDecimal nomoney:null, BigDecimal[] moneys")
	public String testParamsBigDecimal(String name, BigDecimal money, BigDecimal nomoney, BigDecimal[] moneys) {
		return ("name=" + name
				+ ", money=" + money + ", nomoney=" + nomoney + ", moneys=" + Arrays.toString(moneys)
		);
	}
}
