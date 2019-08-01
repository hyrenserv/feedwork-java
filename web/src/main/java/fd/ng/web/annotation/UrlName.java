package fd.ng.web.annotation;

import java.lang.annotation.*;


/**
 * Action 中，如果需要定义同名的方法，需要使用本注解定义别名，用于URL访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UrlName {
	String value(); // 对Action方法映射的新名字。注意：仅仅是对方法起别名，并不能改变整个URL，也就是不能包含 /
}
