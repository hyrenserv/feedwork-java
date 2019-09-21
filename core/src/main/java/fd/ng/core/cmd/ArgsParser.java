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
	// key: 命令行参数名 , value: 这个参数对应的预定义信息Option
	private final Map<String, Option> definedOptions = new HashMap<>(); // 初始定义的参数清单
	private final Set<String> requiredOptions = new HashSet<>(); // 存储必须存在的参数名
	private final Map<String, Option> cmdInputOption = new HashMap<>(); // 存储命令行输入的名值对，值可为空

	public ArgsParser() {

	}

	/**
	 * 添加一个预定义的参数项。name=value格式
	 *
	 * @param name       参数名
	 * @param required   是否为必输参数
	 * @param remark     该参数使用说明
	 * @return
	 */
	public ArgsParser defOptionPair(String name, boolean required, String remark) {
		Validator.notEmpty(name);

		Option option = new Option(name, required, false, remark);
		definedOptions.put(name, option);
		if(required)
			requiredOptions.add(name);
		return this;
	}

	/**
	 * 添加一个预定义的参数项。无值的开关参数
	 *
	 * @param name       参数名
	 * @param required   是否为必输参数
	 * @param remark     该参数使用说明
	 * @return
	 */
	public ArgsParser defOptionSwitch(String name, boolean required, String remark) {
		Validator.notEmpty(name);

		Option option = new Option(name, required, true, remark);
		definedOptions.put(name, option);
		if(required)
			requiredOptions.add(name);
		return this;
	}

	/**
	 * 解析命令行参数
	 * @param args String[] 即：main函数的输入参数
	 */
	public ArgsParser parse(String[] args) {
		if(args.length==1&&args[0].trim().equalsIgnoreCase("-help")) {
			System.out.println(usage());
			System.exit(-1);
		}
		Set<String> argNames = new HashSet<>(args.length); // 存储每个参数的名字
		for(String ele : args) {
			int loc = ele.indexOf('=');
			if(loc<1) { // 无值的参数
				cmdInputOption.put(ele, new Option(definedOptions.get(ele), null));
				argNames.add(ele);
			} else {
				String name  = ele.substring(0, loc);
				String value = ele.substring(loc+1).trim();
				if(value.length()>2&&value.charAt(0)=='"'&&value.charAt(value.length()-1)=='"') {
					String ts = value.substring(1, value.length() - 2); // 去掉前后双引号
					if(ts.contains("\\\"")) // 这是一个被前后双引号包裹的值。需要去掉转义字符
						value = ts.replace("\\\"", "\"");
				}
				cmdInputOption.put(name, new Option(definedOptions.get(name), value));
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
	public Option opt(String name) {
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
			String required = v.required?"[required]":"          ";
			String cc = v.isSwitch?"    ":"=...";
			usage.append(String.format("*  %10s%s  %s %s %n", v.name, cc, required, v.remark));
		});
		usage.append(String.format("*%n"));
		usage.append(String.format("%42s%n%n", "*").replace(' ', '*'));
		return usage.toString();
	}

	/**
	 *
	 * @param argKeys 每个参数的名字
	 */
	private void checkArgKey(Set<String> argKeys) {
		List<String> missingKeys = new ArrayList<>(argKeys.size());
		for(String opName : requiredOptions) {
			if(!argKeys.contains(opName)) missingKeys.add(opName);
		}
		if(missingKeys.size()>0) {
//			throw new IllegalArgumentException("缺少了必要的输入参数：" + missingKeys.toString() + "\n" + usage());
			System.out.println("\n缺少了必要的输入参数：" + missingKeys.toString());
			System.out.println(usage());
			System.exit(-1);
		}
	}

	public static class Option {
		public static final Option NONE = new Option();
		public final String    name;
		public final String    value;
		public final String    remark;
		public final boolean   required;
		public final boolean   isSwitch;

		private Option() {
			this.name = null;
			this.value = null;
			this.required = false;
			this.isSwitch = false;
			this.remark = null;
		}
		/**
		 * 用于构造命令行输入值的Option
		 * @param op 预定义的op
		 * @param value
		 */
		public Option(final Option op, String value) {
			this.name = op.name;
			this.value = value;
			this.remark = null; // 这是解析了命令行参数后的Option对象，remark可以用name到definedOptions中获取。省点内存占用而已
			this.isSwitch = op.isSwitch;
			this.required = op.required;
		}

		/**
		 * 用于构造初始预定义的Option
		 * @param name 参数名
		 * @param required 该参数是否必须提供
		 * @param isSwitch 该参数是否为开关变量（不需要值）
		 * @param remark 该参数的说明
		 */
		public Option(String name, boolean required, boolean isSwitch, String remark) {
			this.name = name;
			this.value = null;
			this.remark = remark==null?"":remark;
			this.required = required;
			this.isSwitch = isSwitch;
		}
		public boolean is(String val) {
			if(isSwitch) throw new FrameworkRuntimeException("Can not use this method for arguments : " + name);
			if(value==null)
				return val==null;
			else
				return value.equals(val);
		}
		public boolean isIgnoreCase(String val) {
			if(isSwitch) throw new FrameworkRuntimeException("Can not use this method for arguments : " + name);
			if(value==null)
				return val==null;
			else
				return value.equalsIgnoreCase(val);
		}
		public boolean exist() {
			return !notExist();
		}
		public boolean notExist() {
			return this==Option.NONE;
		}
		@Override
		public String toString() {
			return "name="+name+", value="+value+", required="+required+", isSwitch="+isSwitch+", remark="+remark;
		}
	}

	// file=/tmp/test.log rw.type=r -fw
	public static void main(String[] args) {
		String arg_file = "file";
		String arg_type = "rw.type";
		String arg_fw   = "-fw";
		ArgsParser CMD_ARGS = new ArgsParser()
				.defOptionPair(arg_file, true, "测试的文件名")
				.defOptionPair("hh", false, "随机数")
				.defOptionPair(arg_type, true, "读写类型，取值范围：r|w|rw")
				.defOptionSwitch(arg_fw, false, "是否自动执行 flush")
				.parse(args);
		System.out.printf("value=%s | arg=%s", CMD_ARGS.opt("file").value, (arg_file));
		System.out.printf("value=%s | arg=%s %n", CMD_ARGS.opt("rw.type").value, (arg_type));
		System.out.printf("exist=%s | arg=%s %n", CMD_ARGS.opt("rw.type").exist(), (arg_type));
		System.out.printf("Case==%s | arg=%s %n", CMD_ARGS.opt("rw.type").isIgnoreCase(arg_type.toUpperCase()), (arg_type));
		System.out.printf("exist=%s | arg=%s %n", CMD_ARGS.opt("-fw").exist(), (arg_fw));
	}
}
