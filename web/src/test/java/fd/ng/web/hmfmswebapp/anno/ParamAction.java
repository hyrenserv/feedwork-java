package fd.ng.web.hmfmswebapp.anno;

import fd.ng.core.utils.StringUtil;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.annotation.RequestParam;
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
	public String testParamsPerson(String name, HttpServletRequest request, int age,
	                               @RequestBean Person person, BigDecimal money,
	                               @RequestParam(ignore = true) String igore_info) {
		String result = "name=" + name + ", age=" + age + ", arg_money=" + money
				+ ", req_money=" + ((request==null)?"no_request":request.getParameter("money"))
				+ ", person=" + person;
		if(StringUtil.isNotEmpty(igore_info)) result += ", igore_info=" + igore_info;
		return result;
	}

	public String testParamsBeanAndEntity(OneFeedBean oneFeedBean, OneTableEntity oneTableEntity,
	                                      String name, @RequestParam(name = "age") String age1) {
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
	public String testParamsString(
			String name, HttpServletRequest request,
			@RequestParam(name = "age") String age1, @RequestParam(nullable = true) String agenull, @RequestParam(valueIfNull = {"1"}) String agedefault,
			String[] ages, @RequestParam(nullable = true) String[] agesnull, @RequestParam(valueIfNull = {"2","3"}) String[] agesdefault ) {
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
	public String testParamsInt(
			String name, HttpServletRequest request,
			int age, @RequestParam(valueIfNull = {"0"}) int agenull, @RequestParam(valueIfNull = {"1"}) int agedefault,
			int[] ages, @RequestParam(valueIfNull = {"0", "0"}) int[] agesnull, @RequestParam(valueIfNull = {"2","3"}) int[] agesdefault ) {
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
	public String testParamsInteger(
			String name, HttpServletRequest request,
			Integer age, @RequestParam(nullable = true) Integer agenull, @RequestParam(valueIfNull = {"1"}) Integer agedefault,
			Integer[] ages, @RequestParam(nullable = true) Integer[] agesnull, @RequestParam(valueIfNull = {"2","3"}) Integer[] agesdefault ) {
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
	public String testParamsBigDecimal(
			String name, HttpServletRequest request,
			BigDecimal age, @RequestParam(nullable = true) BigDecimal agenull, @RequestParam(valueIfNull = {"1"}) BigDecimal agedefault,
			BigDecimal[] ages, @RequestParam(nullable = true) BigDecimal[] agesnull, @RequestParam(valueIfNull = {"2","3"}) BigDecimal[] agesdefault ) {
		String result = ("name=" + name + ", sex=" + request.getParameter("sex")
				+ ", sexes=" + Arrays.toString(request.getParameterValues("sexes"))
				+ ", age=" + age + ", agenull=" + agenull + ", agedefault=" + agedefault
				+ ", ages=" + Arrays.toString(ages) + ", agesnull=" + Arrays.toString(agesnull) + ", agesdefault=" + Arrays.toString(agesdefault)
		);
		return result;
	}
}
