package fd.ng.core.annotation;

public class ParamValue {
	public final String name; // 值为方法的参数名字
	public final String alias; // 参数别名。比如，在WEB中，对应 request 中名字
	public final boolean nullable; // 该参数是否可以不存在或为空串。
	public final String[] valueIfNull; // 对于可空变量，赋予的默认值（因为前端提交的数据有可能是字符数组）
	public final boolean ignore; // 是否忽略（跳过）被注解的参数
	public final boolean isBean; // 该参数是否为JavaBean

	public ParamValue(String name, String alias, boolean nullable, String[] valueIfNull, boolean isBean, boolean ignore) {
		this.name = name;
		this.alias = alias;
		this.nullable = nullable;
		this.valueIfNull = valueIfNull;
		this.isBean = isBean;
		this.ignore = ignore;
	}
}
