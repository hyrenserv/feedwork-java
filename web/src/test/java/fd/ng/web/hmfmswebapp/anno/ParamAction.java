package fd.ng.web.hmfmswebapp.anno;

import fd.ng.core.annotation.Param;
import fd.ng.core.utils.StringUtil;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.hmfmswebapp.a0101.Person;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 用于测试注解： RequestParam 和 RequestBean。
 * {@link ParamActionTest}
 */
public class ParamAction extends WebappBaseAction {
	/**
	 * 构造 POST 数据：
	 * 		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
	 * 		requestParameter.put( "name", new String[]{"testParamsPerson"} );
	 * 		requestParameter.put( "sex", new String[]{"male"} ); // sex允许空，但是给他值
	 * 		requestParameter.put( "age", new String[]{"777"} );
	 * 		requestParameter.put( "money", new String[]{"10001.01"} );
	 */
	@Param(name = "name",desc = "test", range = "..")
	@Param(name = "request",desc = "test", range = "..")
	@Param(name = "age",desc = "test", range = "..")
	@Param(name = "person",isBean = true,desc = "test", range = "..")
	@Param(name = "money",desc = "test", range = "..")
	@Param(name = "igore_info",desc = "test", range = "..", ignore = true)
	public String testParamsPerson(String name, HttpServletRequest request, int age,
	                               Person person, BigDecimal money,
	                               String igore_info) {
		String result = "name=" + name + ", age=" + age + ", arg_money=" + money
				+ ", req_money=" + ((request==null)?"no_request":request.getParameter("money"))
				+ ", person=" + person;
		if(StringUtil.isNotEmpty(igore_info)) result += ", igore_info=" + igore_info;
		return result;
	}

	@Param(name = "oneFeedBean",isBean = true, desc = "test", range = "..")
	@Param(name = "oneTableEntity",isBean = true,desc = "test", range = "..")
	@Param(name = "age1", alias = "age", desc = "test", range = "..")
	@Param(name = "name",desc = "test", range = "..")
	public String testParamsBeanAndEntity(OneFeedBean oneFeedBean, OneTableEntity oneTableEntity,
	                                      String name, String age1) {
		String result = "name=" + name + ", age=" + age1
				+ ", oneFeedBean=" + oneFeedBean
				+ ", oneTableEntity=" + oneTableEntity;
		return result;
	}

	/**
	 * 构造 POST 数据：
	 * 		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
	 * 		requestParameter.put( "name", new String[]{"testParamsString"} );
	 * 		requestParameter.put( "sex", new String[]{"male"} );
	 * 	    requestParameter.put( "sexes", new String[]{"男", "女"} );
	 * 		requestParameter.put( "age", new String[]{"777"} );
	 * 		requestParameter.put( "ages", new String[]{"10", "11", "12"} );
	 */
	@Param(name = "name",desc = "test", range = "..")
	@Param(name = "request",desc = "test", range = "..")
	@Param(name = "age1",alias = "age",desc = "test", range = "..")
	@Param(name = "agenull",nullable = true, desc = "test", range = "..")
	@Param(name = "agedefault",valueIfNull = {"1"}, desc = "test", range = "..")
	@Param(name = "ages",desc = "test", range = "..")
	@Param(name = "agesnull",nullable = true, desc = "test", range = "..")
	@Param(name = "agesdefault",valueIfNull = {"2", "3"}, desc = "test", range = "..")
	public String testParamsString(
			String name, HttpServletRequest request,
			String age1, String agenull, String agedefault,
			String[] ages, String[] agesnull, String[] agesdefault ) {
		String result = ("name=" + name + ", sex=" + request.getParameter("sex")
				+ ", sexes=" + Arrays.toString(request.getParameterValues("sexes"))
				+ ", age=" + age1 + ", agenull=" + agenull + ", agedefault=" + agedefault
				+ ", ages=" + Arrays.toString(ages) + ", agesnull=" + Arrays.toString(agesnull) + ", agesdefault=" + Arrays.toString(agesdefault)
		);
		return result;
	}

	/**
	 * 构造 POST 数据：
	 * 		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
	 * 		requestParameter.put( "name", new String[]{"testParamsInt"} );
	 * 		requestParameter.put( "sex", new String[]{"male"} );
	 * 	    requestParameter.put( "sexes", new String[]{"男", "女"} );
	 * 		requestParameter.put( "age", new String[]{"777"} );
	 * 		requestParameter.put( "ages", new String[]{"10", "11", "12"} );
	 */
	@Param(name = "name",desc = "test", range = "..")
	@Param(name = "request",desc = "test", range = "..")
	@Param(name = "age",desc = "test", range = "..")
	@Param(name = "agenull",valueIfNull = {"0"}, desc = "test", range = "..")
	@Param(name = "agedefault",valueIfNull = {"1"}, desc = "test", range = "..")
	@Param(name = "ages",desc = "test", range = "..")
	@Param(name = "agesnull",valueIfNull = {"0", "0"}, desc = "test", range = "..")
	@Param(name = "agesdefault",valueIfNull = {"2", "3"}, desc = "test", range = "..")
	public String testParamsInt(
			String name, HttpServletRequest request,
			int age, int agenull, int agedefault,
			int[] ages, int[] agesnull, int[] agesdefault ) {
		String result = ("name=" + name + ", sex=" + request.getParameter("sex")
				+ ", sexes=" + Arrays.toString(request.getParameterValues("sexes"))
				+ ", age=" + age + ", agenull=" + agenull + ", agedefault=" + agedefault
				+ ", ages=" + Arrays.toString(ages) + ", agesnull=" + Arrays.toString(agesnull) + ", agesdefault=" + Arrays.toString(agesdefault)
		);
		return result;
	}

	/**
	 * 构造 POST 数据：
	 * 		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
	 * 		requestParameter.put( "name", new String[]{"testParamsInteger"} );
	 * 		requestParameter.put( "sex", new String[]{"male"} );
	 * 	    requestParameter.put( "sexes", new String[]{"男", "女"} );
	 * 		requestParameter.put( "age", new String[]{"777"} );
	 * 		requestParameter.put( "ages", new String[]{"10", "11", "12"} );
	 */
	@Param(name = "name",desc = "test", range = "..")
	@Param(name = "request",desc = "test", range = "..")
	@Param(name = "age",desc = "test", range = "..")
	@Param(name = "agenull",nullable = true, desc = "test", range = "..")
	@Param(name = "agedefault",valueIfNull = {"1"}, desc = "test", range = "..")
	@Param(name = "ages",desc = "test", range = "..")
	@Param(name = "agesnull",nullable = true, desc = "test", range = "..")
	@Param(name = "agesdefault",valueIfNull = {"2", "3"}, desc = "test", range = "..")
	public String testParamsInteger(
			String name, HttpServletRequest request,
			Integer age, Integer agenull, Integer agedefault,
			Integer[] ages, Integer[] agesnull, Integer[] agesdefault ) {
		String result = ("name=" + name + ", sex=" + request.getParameter("sex")
				+ ", sexes=" + Arrays.toString(request.getParameterValues("sexes"))
				+ ", age=" + age + ", agenull=" + agenull + ", agedefault=" + agedefault
				+ ", ages=" + Arrays.toString(ages) + ", agesnull=" + Arrays.toString(agesnull) + ", agesdefault=" + Arrays.toString(agesdefault)
		);
		return result;
	}

	/**
	 * 构造 POST 数据：
	 * 		Map<String, String[]> requestParameter = new HashMap<>(); // 模拟request中的请求参数
	 * 		requestParameter.put( "name", new String[]{"testParamsBigDecimal"} );
	 * 		requestParameter.put( "sex", new String[]{"male"} );
	 * 	    requestParameter.put( "sexes", new String[]{"男", "女"} );
	 * 		requestParameter.put( "age", new String[]{"777"} );
	 * 		requestParameter.put( "ages", new String[]{"10", "11", "12"} );
	 */
	@Param(name = "name",desc = "test", range = "..")
	@Param(name = "request",desc = "test", range = "..")
	@Param(name = "age",desc = "test", range = "..")
	@Param(name = "agenull",nullable = true, desc = "test", range = "..")
	@Param(name = "agedefault",valueIfNull = {"1"}, desc = "test", range = "..")
	@Param(name = "ages",desc = "test", range = "..")
	@Param(name = "agesnull",nullable = true, desc = "test", range = "..")
	@Param(name = "agesdefault",valueIfNull = {"2", "3"}, desc = "test", range = "..")
	public String testParamsBigDecimal(
			String name, HttpServletRequest request,
			BigDecimal age, BigDecimal agenull, BigDecimal agedefault,
			BigDecimal[] ages, BigDecimal[] agesnull, BigDecimal[] agesdefault ) {
		String result = ("name=" + name + ", sex=" + request.getParameter("sex")
				+ ", sexes=" + Arrays.toString(request.getParameterValues("sexes"))
				+ ", age=" + age + ", agenull=" + agenull + ", agedefault=" + agedefault
				+ ", ages=" + Arrays.toString(ages) + ", agesnull=" + Arrays.toString(agesnull) + ", agesdefault=" + Arrays.toString(agesdefault)
		);
		return result;
	}
}
