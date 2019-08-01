package fd.ng.web.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

/**
 * 按注解中的顺序依次给方法的参数赋值。
 * 例如方法：
 *
 *  {@Params}("String username, int age, String sex:null, String[] addr")
 *  public void addUser(String name, int age, String sex, String[] addr) {}
 * 以上注解相当于执行了如下操作：
 *  name = request.getParameter("username");            不允许空。如果空则抛出参数不合法异常
 *  age = new Integer(request.getParameter("age"));     不允许空。如果空则抛出参数不合法异常
 *  sex = request.getParameter("sex");                  允许把sex赋值为null
 *  addr = request.getParameterValues("addr");          不允许空。如果空则抛出参数不合法异常
 *
 *  除了主类型参数外，还可以使用 HttpServletRequest 和 RequestAutowiredBean注解的JavaBean（注意，必须有空的构造函数）
 *  {@Params}("String username, HttpServletRequest request, int age, Person person")
 * 	public String testParamsPerson(String name, HttpServletRequest request, int age, Person person, BigDecimal money) {}
 *
 *  注意：
 *  1）注解中的String, int等类型写不写都可以。例子中保留了类型是因为从方法中直接拷贝过去比较省事。
 *  2）注解中的参数个数与方法参数个数可以不一致。不够时，多出来的参数都被赋值为null。超出的话，多余的被忽略。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Params {
	String value() default StringUtil.EMPTY;
}
