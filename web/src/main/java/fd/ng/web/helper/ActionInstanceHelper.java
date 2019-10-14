package fd.ng.web.helper;

import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.ParamValue;
import fd.ng.core.annotation.Params;
import fd.ng.core.conf.AppinfoConf;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.ArrayUtil;
import fd.ng.core.utils.ClassUtil;
import fd.ng.core.utils.StopWatch;
import fd.ng.core.utils.StringUtil;
import fd.ng.web.action.AbstractBaseAction;
import fd.ng.web.annotation.Action;
import fd.ng.web.annotation.Inject;
import fd.ng.web.annotation.UrlName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局实例化WEB层的功能性对象。
 * 包括所有的Action等
 */
public final class ActionInstanceHelper
{
	private static final Logger logger = LogManager.getLogger(ActionInstanceHelper.class.getName());
	// 只有以基础包开始的uri才需要对应具体的Action做处理
	public static final String HEAD_URI = "/"+ AppinfoConf.AppBasePackage.replaceAll("\\.", "/");
	// 存储所有的 Action 类的实例化对象。 key：Action包名或注解 UriExt 的值，value：Action实例对象
	private static Map<String, Object> allActionMap = new HashMap<>();
	// key: uri全路径（即包名+方法名）， value: map（方法名， 方法对象）
	private static Map<String, Map<String, Method>> allActionMethodMap = new HashMap<>();
	// key: Action的每个方法， value: map（参数名， 参数注解的值）
	private static Map<Method, Map<String, ParamValue>> allActionMethodParamMap = new HashMap<>();
	static{
		try{
			logger.debug("Instance Action ... ...");
			boolean hasError = false;
			// 不能通过基类来判断。因为多层继承的情况下，中间的类很可能不需要实例化，甚至中间的类可能是抽象类
			// 所以，必须通过注解来判断哪些类需要被实例化
			StopWatch stopWatch = new StopWatch();
			List<Class<?>> actionClassList = ClassUtil.getClassListByAnnotation(AppinfoConf.AppBasePackage, Action.class);
			logger.trace(actionClassList.stream().map(Class::getName).map(name->name.replace(AppinfoConf.AppBasePackage, "")).collect(Collectors.joining(", ","所有的Action：", "")));
			stopWatch.start("Instance Action");
			Map<Class<?>, String> wrongActions = new HashMap<>(); // 记录每个有问题的Action及错误提示信息。
			for(final Class<?> actionClass : actionClassList) {
				final String className = actionClass.getName();
				logger.trace("============= Current Class : {} =============", className);
				if(Modifier.isAbstract(actionClass.getModifiers())){
					// 过滤掉抽象类（Action的父类都应该定义为抽象类，否则会被实例化
					logger.trace("SKIP Abstract class : {}", className);
					continue;
				}
				Action actionAnnotation = actionClass.getAnnotation(Action.class); // 得到该类的注解
				// 1）获得 Action 类的包名或注解内的 UriExt，作为该类的Map KEY
				final String packageName = actionClass.getPackage().getName();
				String actionLookupKey = actionAnnotation.UriExt(); // allActionMap 的key,
				String actionLookupKeyType;
				if(StringUtil.isEmpty(actionLookupKey)) { // 使用包名作为 UriExt
					if(StringUtil.isEmpty(packageName)) throw new FrameworkRuntimeException("class(="+className+")'s package name is null!");
					actionLookupKey = "/" + packageName.replace(".", "/");
					actionLookupKeyType = "Package";
				} else { // 定义了 UriExt 属性
					if(actionLookupKey.startsWith("/")||actionLookupKey.endsWith("/")) { // 不能用 '/' 开头或结尾
						hasError = true;
						logger.error("Action({}) 的 UriExt 属性不能以'/'开头或结尾！", className);
					} else if(actionLookupKey.startsWith("^/")) { // UriExt的值作为全路径使用
						actionLookupKey = actionLookupKey.substring(1);
					} else {
						// 把 UriExt 的值追加到包名路径后面
						actionLookupKey = "/" + packageName.replace(".", "/") + "/" + actionLookupKey;
					}
					actionLookupKeyType = "UriExt";
				}
				logger.trace("current class [{}], lookupkey={}", className, actionLookupKey);

				// 2.1）记录每个不合法 Action 的错误（比如有成员变量等）
				Set<String> wrongFields = getFieldsNotStaticFinal(actionClass);
				if(wrongFields.size()>0) {
					String errMsg = " 以下成员变量必须定义为static final类型：" +
							wrongFields.stream().collect(Collectors.joining(", ", "[ ", " ]"));
					putActionErrorMsg(wrongActions, actionClass, errMsg);
					wrongFields.clear();
				}
				// 2.2）得到该 Action 类的所有 public 方法并存下来
				// 存储当前 Action 类所有方法的容器
				Map<String, Method> actionMethodMap = new HashMap<>();
//				// 存储当前 Action 类所有方法的参数信息的容器
//				Map<String, Map<String, ParamValue>> actionMethodParamMap = new HashMap<>();
				// 得到当前 Action 类的所有方法
				Method[] actionMethods = actionClass.getDeclaredMethods();
				if(ArrayUtil.isEmpty(actionMethods)) {
					logger.warn("class [{}] has not any declared method, be skipped!", className);
					continue;
				}
				// 处理来自基类的内置方法 _doPre _doPost _doException
				Optional.ofNullable(getMethodThroughSuperClass(actionClass, AbstractBaseAction.PreProcess_MethodName))
						.ifPresent(m->actionMethodMap.put(AbstractBaseAction.PreProcess_MethodName, m));
				Optional.ofNullable(getMethodThroughSuperClass(actionClass, AbstractBaseAction.PostProcess_MethodName))
						.ifPresent(m->actionMethodMap.put(AbstractBaseAction.PostProcess_MethodName, m));
				Optional.ofNullable(getMethodThroughSuperClass(actionClass, AbstractBaseAction.ExceptionProcess_MethodName))
						.ifPresent(m->actionMethodMap.put(AbstractBaseAction.ExceptionProcess_MethodName, m));

				// 获取当前 Action 的所有public方法
				StringJoiner wrongMethods = new StringJoiner("\n");
				for (Method actionMethod : actionMethods) {
					int methodModifier = actionMethod.getModifiers();
					if(Modifier.isAbstract(methodModifier)||Modifier.isStatic(methodModifier)) continue; // 抽象和静态方法不处理
					if(Modifier.isPublic(methodModifier)) { // 只检查 public 的方法
						String curMethodName = actionMethod.getName();
						UrlName urlName = actionMethod.getAnnotation(UrlName.class);
						if(urlName !=null) curMethodName = urlName.value();
						if(curMethodName.contains("/")) {
							wrongMethods.add(String.format("方法或UrlMethod注解：%s, 不允许使用 / 等特殊字符", curMethodName));
						} else if(actionMethod.isVarArgs()) {
							wrongMethods.add(String.format("方法：%s 不允许使用可变参数", curMethodName));
						} else if(actionMethodMap.containsKey(curMethodName)) { // 存在相同名字的方法是不合法的，存下来
							wrongMethods.add(String.format("方法名：%s 已经存在！如果是同名方法，请使用UrlName注解。如果是UrlName注解重名，请更换其他名字", curMethodName));
							actionMethodMap.remove(curMethodName);
						} else {
							actionMethodMap.put(curMethodName, actionMethod);

							// 获取该方法的参数注解信息
							if(actionMethod.getParameters().length<1) continue; // 该方法无参数，不需要处理注解

							Param[] params;
							Params paramsAnno = actionMethod.getAnnotation(Params.class);
							if(paramsAnno==null) {// 该方法没有多个注解
								Param paramAnno = actionMethod.getAnnotation(Param.class);
								if(paramAnno==null) //该方法一个注解都没有
									throw new Error(String.format("方法：%s 没有设置【Param】注解？",
											actionMethod.getName()));
								params = new Param[]{paramAnno};
							} else {
								params = paramsAnno.value();
							}
							Map<String, ParamValue> paramValueMap = new HashMap<>(params.length);
							for(Param param : params) {
								ParamValue paramValue = new ParamValue(param.name(), param.alias(), param.nullable(),
										param.valueIfNull(), param.isBean(), param.ignore());
								paramValueMap.put(param.name(), paramValue);
							}
							// 检查各个参数与注解是否一一匹配
							checkParamAnno(actionClass, actionMethod, paramValueMap);
							//TODO 检查返回值的注解是否匹配
//							actionMethodParamMap.put(curMethodName, paramValueMap);
							allActionMethodParamMap.put(actionMethod, paramValueMap);
						}
					}
				}
				if(wrongMethods.length()>2) {
					putActionErrorMsg(wrongActions, actionClass, wrongMethods.toString());
				}
				logger.trace("  --> public methods : {}", actionMethodMap.keySet().stream().collect(Collectors.joining(", ")) );

				// 3）实例化 Action 类，并缓存类中的每个方法
				if(actionMethodMap.size()>0) { // 该 Action 类有public方法，才实例化并缓存该类及其public方法
					// 检查是否已经有相同 key 的 Action
					Object existActionInstance = allActionMap.get(actionLookupKey);
					if(existActionInstance!=null){
						hasError = true;
						if("Package".equals(actionLookupKeyType)) { // Action注解，使用包名作为 KEY
							logger.error("同一个包中只能有一个Action，多个Action请使用@Action注解 UrlName 属性。" +
									"package [{}] actions : {}, {}",
									packageName, actionClass.getSimpleName(), existActionInstance.getClass().getSimpleName());
						}
						else { // 使用的Action注解的 UrlName 属性
							logger.error(
									"Action(" + className + ")'s  @Action(UrlName=\""+actionLookupKey+"\")  has been used by ["+existActionInstance.getClass().getName()+"]");
						}
					} else {
						Object actionInstance = actionClass.newInstance();
						allActionMap.put(actionLookupKey, actionInstance);
						allActionMethodMap.put(actionLookupKey, Collections.unmodifiableMap(actionMethodMap));
//						allActionMethodParamMap.put(actionLookupKey, Collections.unmodifiableMap(actionMethodParamMap));
					}
				}
			}
			stopWatch.stopShow();
			allActionMap = Collections.unmodifiableMap(allActionMap);
			allActionMethodMap = Collections.unmodifiableMap(allActionMethodMap);
			allActionMethodParamMap = Collections.unmodifiableMap(allActionMethodParamMap);

			/** 【错误排查】 */
			// 对Action实例化清单循环一遍，如果该类没有任何public方法，那么强制提示声明为抽象类。
			// 最主要的目的是，对于这种没有public方法的类，一般是父类，不应该被实例化
			stopWatch.start("Checking error");
			final List<Class<?>> illeglClass = new ArrayList<>();

			allActionMap.forEach((k, obj)->{
				Set<Method> actionPublicMethods = ClassUtil.getAllMethods(obj.getClass(), Modifier.PUBLIC, false); // 当前action的所有方法
				if(actionPublicMethods.size()<1) // 该类没有任何public方法
					illeglClass.add(obj.getClass());
			});
			if(illeglClass.size()>0) {
				logger.error("以下Action类没有定义任何public方法，必须设置为抽象类：[ {} ]！",
						illeglClass.stream().map(Class::getName).collect(Collectors.joining(", ")));
				hasError = true;
			}
			// 有问题的action处理
			if(wrongActions.size()>0) hasError = true;
			wrongActions.forEach((k,v)->{
				logger.error("[ {}] {}", k.getSimpleName(), v);
			});
			stopWatch.stopShow();
			if(hasError)
				throw new FrameworkRuntimeException("Load Action Class fail!");
			else {
				if (allActionMap.size() < 5) {
					logger.warn("Only {} actions in this project! ", allActionMap.size());
				}
				if(logger.isTraceEnabled()) {
					StringBuilder sb = new StringBuilder();
					allActionMap.forEach((uri, clzAction) -> {
						sb.append("uri=").append(uri).append(" | action=").append(clzAction.getClass().getSimpleName()).append("\n");
					});
					logger.trace("有效的Action : \n{}", sb.toString());
				}
			}

			// 实例化所有的Manager类，并注入Action类
			stopWatch.start("Inject Action");
			for(String actionKey : allActionMap.keySet()){
				Object actionObj = allActionMap.get(actionKey);
				Field[] fileds = actionObj.getClass().getDeclaredFields();
				for (Field f : fileds) {
					//String name = f.getName();
					if(f.isAnnotationPresent(Inject.class)){
						Class<?> clz = f.getType();
						Object manager = clz.newInstance();
						f.setAccessible(true);
						f.set(actionObj, manager);
						logger.debug("Inject {} to {}", clz.getName(), actionObj.getClass().getName());
					}
				}
			}
			stopWatch.stopShow();
		} catch (Exception e) {
			throw new Error("ActionInstanceHelper static init failed!", e);
		}
	}

	public static Map<String, Object> getAllActionMap() {
		return allActionMap;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAction(String actionKey) {
		return (T) allActionMap.get(actionKey);
	}

	public static Map<String, Map<String, Method>> getActionAllMethodMap(String actionKey) {
		return allActionMethodMap;
	}

	/**
	 * 用 uri 和 方法名，获得该方法的 Method 对象。
	 * @param actionKey String Action 类的包名(.被替换为/)或注解内的 UriExt。实际就是访问地址中的 pathInfo。
	 * @param methodName String 方法名
	 * @return Method
	 */
	public static Method getActionMethod(String actionKey, String methodName){
		Map<String, Method> map = allActionMethodMap.get(actionKey);
		if(map == null) return null;
		else return map.get(methodName);
	}

	public static Map<String, ParamValue> getActionMethodParamMap(Method method){
		return allActionMethodParamMap.get(method);
	}

	/**
	 * 获取 Method 对象。包含父类的 protected 方法，不包含 private 方法
	 * @param actionClass
	 * @param methodName
	 * @return
	 */
	private static Method getMethodThroughSuperClass(Class<?> actionClass, String methodName) {
		try {
			if(actionClass==null) return null;
			Method method = actionClass.getDeclaredMethod(methodName, HttpServletRequest.class);
			if( Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()) )
				return method;
			else
				return null;
		} catch (NoSuchMethodException e) {
			return getMethodThroughSuperClass(actionClass.getSuperclass(), methodName);
		}
	}

	/**
	 * 得到一个类的所有非StaticFinal类型的成员变量（包括继承自父类的变量）
	 * @param actionClass
	 * @return
	 */
	private static Set<String> getFieldsNotStaticFinal(Class<?> actionClass) {
		List<Field> fields = ClassUtil.getAllVisibleFields(actionClass);
		final Set<String> result = new HashSet<>();
		fields.forEach(f->{
			if( !(Modifier.isFinal(f.getModifiers())&&Modifier.isStatic(f.getModifiers())) ) {
				result.add( f.getName() );
			}
		});
		return result;
	}

	// 检查方法的每个注解中的name是否与参数匹配
	private static void checkParamAnno(Class<?> actionClass, Method actionMethod, Map<String, ParamValue> paramMap) {
		Parameter[] parameters = actionMethod.getParameters();
		StringBuilder errMsg = new StringBuilder();
		for(Parameter parameter : parameters) {
			String paramName = parameter.getName();
			if(!paramMap.containsKey(paramName)) {
				errMsg.append(String.format("  Param [%-10s] no matched Param Annotation.%n", paramName));
			}
		}
		if(errMsg.length()>0)
			throw new Error(String.format("Action [%s] 's Method [%s] Param Error :%n%s",
					actionClass.getSimpleName(), actionMethod.getName(), errMsg.toString()));
	}
	private static void putActionErrorMsg(Map<Class<?>, String> wrongActions, Class<?> actionClass, String errMsg) {
		if(wrongActions.containsKey(actionClass)) {
			String wrongActionInfo = wrongActions.get(actionClass);
			wrongActions.put(actionClass, wrongActionInfo+"\n"+errMsg);
		} else {
			wrongActions.put(actionClass, " 发生的错误：\n"+errMsg);
		}
	}
}
