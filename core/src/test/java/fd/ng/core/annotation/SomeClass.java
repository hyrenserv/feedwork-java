package fd.ng.core.annotation;

public class SomeClass {

//	public String say(
//			@Param1(desc = "用户的年龄",
//					range = "大于0，小于200"
//			) int age,
//			@Param1(desc = "用户的兴趣爱好（码值）",
//					range = "对于代码项：Favor"
//			) String[] favor,
//			@Param1(desc = "用户家庭住址",
//					range = "格式为：路、弄、只弄、号",
//					nullable = true
//			) String addr)
//	{
//		return ("Hello World!");
//	}
//
	@Method(desc = "这个方法的功能说明",
			logicStep ="1. lkjksldjf" +
					"2. ljlkjklj" +
					"3. ljlkjklj")
	@Param(name="age", desc = "用户的年龄",
			range = "大于0，小于200"
	)
	@Param(name="favor", desc = "用户的兴趣爱好（码值）",
			range = "对于代码项：Favor"
	)
	@Param(name="addr", desc = "用户家庭住址",
			range = "格式为：路、弄、只弄、号",
			nullable = true,
			isBean = false
	)
	@Return(desc = "返回值含义",
			range = "返回值的取值范围")
	public String say1(int age, String[] favor, String addr)
	{
		return ("Hello World!");
	}

	public String say2(int age, String[] favor, String addr)
	{
		return ("Hello World!");
	}

	@Param(desc = "say3:测试仅有一个注解的情况", range = "say3")
	public String say3(int age, String[] favor, String addr)
	{
		return ("Hello World! 3");
	}

	@Param(isBean = true, desc = "say4:测试仅有一个注解的情况", range = "say4")
	public String say4(OneBean bean)
	{
		return ("Hello World! 4");
	}

//	@Comments(name="", desc = "方法说明", range = "使用场景说明")
//	@Params2({
//			@Comments(name="age", desc = "用户的年龄",
//					range = "大于0，小于200"
//			),
//			@Comments(name="favor", desc = "用户的兴趣爱好（码值）",
//					range = "对于代码项：Favor"
//			),
//			@Comments(name="addr", desc = "用户家庭住址",
//					range = "格式为：路、弄、只弄、号"
//			),
//			@Comments(desc = "返回值说明",
//					range = "......"
//			)
//	})
//	public String say2(int age, String[] favor, @Param1(nullable = true) String addr)
//	{
//		return ("Hello World!");
//	}
}

