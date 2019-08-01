package fd.ng.core.cmd;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;

import java.util.*;

/**
 * 解析命令行输入参数。
 * 接受的格式为：
 * 1）名值对：name=value
 * 2）仅有名字
 * 如果值中有空格、制表符等字符，需使用双引号
 * 如果有前后双引号，那么里面双引号需要加转义字符 \
 *
 * 用法：
 * ArgsParser cmd = new ArgsParser()
 *                  .addOption("type", "r|w|d", "读写删除", true)
 *                  ...... 依次把每个参数初始化进来
 *                  .parse(args); // args就是 main 函数的参数
 *
 * 后续代码中，获取命令行输入项： cmd.option("type")
 *
 */
public class ArgsParser {
	private final Map<String, Option> definedOptions = new HashMap<>(); // 初始定义的参数清单
	private final Set<Option> requiredOptions = new HashSet<>(); // 存储必须存在的参数名
	private final Map<String, Option> cmdInputOption = new HashMap<>(); // 存储命令行输入的名值对，值可为空

	public ArgsParser() {

	}

	/**
	 * 添加一个预定义的无值参数项
	 *
	 * @param name          参数名
	 * @param remark        该参数使用说明
	 * @param required      是否为必输参数
	 * @return
	 */
	public ArgsParser addOption(String name, String remark, boolean required) {
		return addOption(name, null, remark, required);
	}

	/**
	 * 添加一个预定义的参数项
	 *
	 * @param name          参数名
	 * @param value         参数值 可空，表示无值参数
	 * @param remark        该参数使用说明
	 * @param required      是否为必输参数
	 * @return
	 */
	public ArgsParser addOption(String name, String value, String remark, boolean required) {
		Validator.notEmpty(name);

		Option option = new Option(name, value, remark, required);
		definedOptions.put(name, option);
		if(required)
			requiredOptions.add(option);
		return this;
	}

	/**
	 * 解析命令行参数
	 * @param args String[] 即：main函数的输入参数
	 */
	public ArgsParser parse(String[] args) {
		Set<String> argNames = new HashSet<>();
		for(String ele : args) {
			int loc = ele.indexOf('=');
			if(loc<1) { // 无值的参数
				cmdInputOption.put(ele, new Option(ele, null, true));
				argNames.add(ele);
			} else {
				String name  = ele.substring(0, loc);
				String value = ele.substring(loc+1).trim();
				if(value.length()>2&&value.charAt(0)=='"'&&value.charAt(value.length()-1)=='"') {
					String ts = value.substring(1, value.length() - 2); // 去掉前后双引号
					if(ts.contains("\\\"")) // 这是一个被前后双引号包裹的值。需要去掉转义字符
						value = ts.replace("\\\"", "\"");
				}
				cmdInputOption.put(name, new Option(ele, value, false));
				argNames.add(name);
			}
		}
		checkArgKey(argNames);
		return this;
	}

	/**
	 * 获取所有“预定义”的 Option
	 * @return Map<String, Option>
	 */
	public Map<String, Option> getDefinedOptions() {
		return definedOptions;
	}

	/**
	 * 根据名字，获取“命令行输入”的 Option
	 * @param name
	 * @return Option
	 */
	public Option option(String name) {
		return cmdInputOption.getOrDefault(name, Option.NONE);
	}

	/**
	 * 根据参数名，判断该参数是否存在（命令行是否输入了该参数）
	 * @param name addOption() 中设置的 '参数的简短别名'
	 * @return
	 */
	public boolean exist(String name) {
		return cmdInputOption.containsKey(name);
	}

	public String usage() {
		final StringBuilder usage = new StringBuilder(String.format("%n%42s%n", "*").replace(' ', '*'));
		usage.append(String.format("*%n"));
		usage.append(String.format("*  Usage :%n"));
		definedOptions.forEach((k, v)->{
			String notNull = v.required?"  不可空":"";
			if(StringUtil.isEmpty(v.value)) // 无值参数
				usage.append("*       ").append(v.name).append(' ').append(v.remark).append(notNull);
			else
				usage.append("*       ").append(v.name).append('=').append(v.value).append(' ').append(v.remark).append(notNull);
			usage.append("\n");
		});
		usage.append(String.format("*%n"));
		usage.append(String.format("%42s%n%n", "*").replace(' ', '*'));
		return usage.toString();
	}

	private void checkArgKey(Set<String> argKeys) {
		List<String> missingKeys = new ArrayList<>();
		for(Option option : requiredOptions) {
			if(!argKeys.contains(option.name)) missingKeys.add(option.name);
		}
		if(missingKeys.size()>0) {
			throw new IllegalArgumentException("缺少了必要的输入参数：" + missingKeys.toString() + "\n" + usage());
		}
	}

	public static class Option {
		public static final Option NONE = new Option(null, null, true);
		public final String    name;
		public final String    value;
		public final String    remark;
		public final boolean   required;
		public final boolean   isNoValue;

		/**
		 * 用于构造命令行输入值的Option
		 * @param name
		 * @param value
		 * @param isNoValue 构建命令行输入的Option时，要根据预定义的name，为这个值设置正确的 isNullable
		 */
		public Option(String name, String value, boolean isNoValue) {
			this.name = name;
			this.value = value;
			this.remark = null;     // 对于命令行输入值来说，这个值设置成什么都应该被忽略。
			this.isNoValue = isNoValue;
			this.required = !isNoValue; // 对于命令行输入值来说，这个值设置成什么都应该被忽略。
		}

		/**
		 * 用于构造初始预定义的Option
		 * @param name
		 * @param value
		 * @param remark
		 * @param required
		 */
		public Option(String name, String value, String remark, boolean required) {
			this.name = name;
			this.value = value;
			this.remark = remark==null?"":remark;
			this.required = required;
			this.isNoValue = StringUtil.isEmpty(value);
		}
		public boolean is(String val) {
			if(isNoValue) throw new FrameworkRuntimeException("Can not use this method for arguments : " + name);
			if(value==null)
				return val==null;
			else
				return value.equals(val);
		}
		public boolean isIgnoreCase(String val) {
			if(isNoValue) throw new FrameworkRuntimeException("Can not use this method for arguments : " + name);
			if(value==null)
				return val==null;
			else
				return value.equalsIgnoreCase(val);
		}
		public boolean exist() {
			return this!=Option.NONE;
		}
		@Override
		public String toString() {
			return "name="+name+", value="+value+", required="+required+", remark="+remark;
		}
	}
}
