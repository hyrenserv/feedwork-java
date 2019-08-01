package fd.ng.web.annotation;

import fd.ng.core.utils.StringUtil;

import java.lang.annotation.*;

/**
 * 被注解的方法参数必须是 DatabaseWrapper 对象。
 *
 * 本注解待完成：
 * 1）组装参数中，使用注解属性构造 DatabaseWrapper.Builder 对象（lazyConnect设置为true！！！）来生成DatabaseWrapper对象
 * 2）把创建的DatabaseWrapper对象存入 request.setAttribute 中
 * 3）在父类中的post和ex中，从request中取出这个对象做相应处理
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DBuilder {
	String dbname(); // 不能空，必须明确写出来，要使用哪个名字建立连接
	String desc() default StringUtil.EMPTY;
//	boolean lazyConnect(); // 不提供这个属性的设置。因为使用本注解定义 Action 方法参数时，必须是lazy方式构建，用户要自己调用 makeConnection 建立连接
	String id() default ValueConstants.DEFAULT_NONE; // 默认值意味着使用系统内部默认规则生成id
	boolean noid() default false;
	boolean autoCommit() default true;
	boolean showsql() default true;
}
