package fd.ng.core.utils;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
	private static final Logger logger = LogManager.getLogger(ClassUtil.class.getName());
	public ClassUtil() { throw new AssertionError("No ClassUtil instances for you!"); }

	public static final char PACKAGE_SEPARATOR_CHAR = '.';
	public static final String PACKAGE_SEPARATOR = String.valueOf(PACKAGE_SEPARATOR_CHAR);

	/**
	 * The inner class separator character: <code>'$' == {@value}</code>.
	 */
	public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
	public static final String INNER_CLASS_SEPARATOR = String.valueOf(INNER_CLASS_SEPARATOR_CHAR);

	/**
	 * Maps a primitive class name to its corresponding abbreviation used in array class names.
	 */
	private static final Map<String, String> abbreviationMap;

	/**
	 * Maps an abbreviation used in array class names to corresponding primitive class name.
	 */
	private static final Map<String, String> reverseAbbreviationMap;

	/**
	 * Feed abbreviation maps
	 */
	static {
		final Map<String, String> m = new HashMap<>();
		m.put("int", "I");
		m.put("boolean", "Z");
		m.put("float", "F");
		m.put("long", "J");
		m.put("short", "S");
		m.put("byte", "B");
		m.put("double", "D");
		m.put("char", "C");
		final Map<String, String> r = new HashMap<>();
		for (final Map.Entry<String, String> e : m.entrySet()) {
			r.put(e.getValue(), e.getKey());
		}
		abbreviationMap = Collections.unmodifiableMap(m);
		reverseAbbreviationMap = Collections.unmodifiableMap(r);
	}
	/**
	 * Maps primitive {@code Class}es to their corresponding wrapper {@code Class}.
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
	static {
		primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		primitiveWrapperMap.put(Character.TYPE, Character.class);
		primitiveWrapperMap.put(Short.TYPE, Short.class);
		primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		primitiveWrapperMap.put(Long.TYPE, Long.class);
		primitiveWrapperMap.put(Double.TYPE, Double.class);
		primitiveWrapperMap.put(Float.TYPE, Float.class);
		primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
	}
	/**
	 * Maps wrapper {@code Class}es to their corresponding primitive types.
	 */
	private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
	static {
		for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
			final Class<?> primitiveClass = entry.getKey();
			final Class<?> wrapperClass = entry.getValue();
			if (!primitiveClass.equals(wrapperClass)) {
				wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
			}
		}
	}

	/**
	 *
	 * @param callerName 调用者的名字，仅用于跟踪调试使用。能知道是谁使用了这个函数
	 * @return
	 */
	public static ClassLoader getClassLoader(String callerName) {
		ClassLoader clzLoader = null;
		try {
			clzLoader = Thread.currentThread().getContextClassLoader();
			if(logger.isTraceEnabled())
				logger.trace("{} Used ClassLoader[ Current Thread ] : {}", callerName, clzLoader);
		}catch (Throwable ex) {
			logger.warn("thread context class loader is null", ex);
		}
		if (clzLoader == null) {
			// 线程class loader为null，则使用本类class loader.
			try {
				clzLoader = ClassUtil.class.getClassLoader();
				logger.warn("{} Used ClassLoader[ ClassUtil.class ] : {}", callerName, clzLoader);
			}catch (Throwable ex) {
				logger.warn("ClassUtil class loader is null.", ex);
			}
			if (clzLoader == null) {
				try {
					clzLoader = ClassLoader.getSystemClassLoader();
					logger.warn("{} Used ClassLoader[ System ClassLoader ] : {}", callerName, clzLoader);
				}
				catch (Throwable ex) {
					logger.error("System ClassLoader Exception.", ex);
				}
			}
		}
		if(clzLoader==null) throw new RawlayerRuntimeException("Can not get ClassLoader!");
		return clzLoader;
	}
	
	public static Class<?> loadClass(String classFullName) throws ClassNotFoundException {
		return loadClass(classFullName, true);
	}
	public static Class<?> loadClass(String classFullName, boolean isInitialize) throws ClassNotFoundException {
		// 不能使用loadClass。因为loadClass不进行包括初始化等一些列步骤，那么静态块和静态对象就不会得到执行
		// return (getClassLoader()).loadClass(className);
		Validator.notNull(classFullName, "Class full name must not be null");
//		try {
		return Class.forName(classFullName, isInitialize, getClassLoader("loadClass("+classFullName+")"));
//		} catch (Exception e) {
//			logger.error(String.format("Class(%s) load failure", classFullName), e);
//			return null;
//		}
	}

	/**
	 * 根据给定的类名（包含包名），实例化类对象。本方法仅支持拥有无参的构造函数的类
	 * @param classFullName
	 * @param <T>
	 * @return
	 */
	public static <T> T newInstance(String classFullName) {
		try {
			Class<?> aClass = loadClass(classFullName);
			// 通过反射创建该实现类对应的实例
			T instance = (T) aClass.newInstance();
			return instance;
		} catch (IllegalAccessException|InstantiationException e) {
			logger.error("newInstance [" + classFullName + "] failed!", e);
			return null;
		} catch (ClassNotFoundException e) {
			logger.warn("clas [" + classFullName + "] not found!", e);
			return null;
		}
	}
	public static <T> T newInstance(Class<T> c) {
		try {
			return c.newInstance();
		} catch (InstantiationException|IllegalAccessException e) {
			logger.error("newInstance ["+c.getName()+"] failed!", e);
			return null;
		}
	}
//	@SuppressWarnings("unchecked")
//	public static <T> T newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
//		return (T) (loadClass(className)).newInstance();
//	}

	public static PropertyDescriptor[] propertyDescriptors(Object o) {
		return propertyDescriptors(o.getClass());
	}
	public static PropertyDescriptor[] propertyDescriptors(Class<?> c) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(c);
			return beanInfo.getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new RawlayerRuntimeException("getPropertyDescriptors for "+c.getName()+" failed", e);
		}
//		return PropertyUtils.getPropertyDescriptors(c);
	}

	/**
	 * Returns whether the given {@code type} is a primitive or primitive wrapper
	 * @param type
	 *            The class to query or null.
	 * @return true if the given {@code type} is a primitive or primitive wrapper
	 */
	public static boolean isPrimitiveOrWrapper(final Class<?> type) {
		if (type == null) {
			return false;
		}
		return type.isPrimitive() || isPrimitiveWrapper(type);
	}
	public static boolean isPrimitiveWrapper(final Class<?> type) {
		return wrapperPrimitiveMap.containsKey(type);
	}
	/**
	 * 判断clsSuper是不是cls的基类。
	 * @param clsSuper
	 * @param cls
	 * @return
	 */
	public static boolean isSuperClass(Class<?> clsSuper, Class<?> cls){
		return clsSuper.isAssignableFrom(cls) && !clsSuper.equals(cls);
	}

	/**
	 * 在开发中经常需要获取资源文件路径，例如读写配置文件等
	 * 获得这个当前程序class文件的根目录。
	 * 比如，WEB程序
	 * @return
	 */
	public static String getClassPath(){
		URL resource = getClassLoader("getClassPath").getResource("");
		if (resource == null)
			return StringUtil.EMPTY;
		else
			return resource.getPath();
	}

	public static List<Class<?>> getClassListByAnnotation(String packageName, Class<? extends Annotation> annotationClass) throws ClassNotFoundException {
		Set<String> classNames = getClassNamesByPackageName(packageName, true);
		if(classNames.isEmpty()) return Collections.emptyList();
		List<Class<?>> classList = new ArrayList<>();
		for(String className : classNames){
			Class<?> cls = ClassUtil.loadClass(className);
			if(cls.isAnnotationPresent(annotationClass)){
				classList.add(cls);
			}
		}
		return classList;
	}

	public static List<Class<?>> getClassListBySuper(String packageName, Class<?> superClass) throws ClassNotFoundException {
		Set<String> classNames = getClassNamesByPackageName(packageName, true);
		if(classNames.isEmpty()) return Collections.emptyList();
		List<Class<?>> classList = new ArrayList<>();
		for(String className : classNames){
//			if(className.startsWith("com.sun.")||className.startsWith("sun.")) {
//				logger.debug("====>    By SuperClass to load class(SKIP) : {}", className);
//				continue;
//			}
			Class<?> cls = ClassUtil.loadClass(className);
			if( superClass.isAssignableFrom(cls) && !superClass.equals(cls) ){
//				logger.debug("====>    By SuperClass to load class : {}", className);
				classList.add(cls);
			}
		}
		return classList;
	}

	/**
	 * 获取某包下所有类（遍历所有子目录）
	 * @param packageName 起始包名。
	 *                    如果为""，这意味着从classpath的根开始找。但是，对于以allInOne的jar来说，无法找到预期的Class！
	 * @return 类的完整名称
	 */
	public static Set<String> getClassNamesByPackageName(String packageName) {
		return getClassNamesByPackageName(packageName, true);
	}
	/**
	 * 获取某包下所有类
	 * @param packageName String 起始包名。可以为空串，这意味着从classpath的根开始找，但是不是为 null
	 * @param isRecursion 是否遍历子包
	 * @return 类的完整名称
	 */
	public static Set<String> getClassNamesByPackageName(String packageName, boolean isRecursion) {
		if(packageName==null) return null;
		Set<String> classNames = null;
		ClassLoader loader = getClassLoader("getClassNamesByPackageName("+packageName+")");
		String packagePath = packageName.replace(".", "/");
		if(StringUtil.isBlank(packageName)) {
			logger.warn("package name is empty string, and when the project starts in a allInOne JAR, the expected class cannot be found.");
		}

		URL url = loader.getResource(packagePath);
		if (url != null) {
			String protocol = url.getProtocol();
			if (protocol.equals("file")) {
				// TODO getPath() 应该使用URLdecode，以便支持中文路径名
				classNames = getClassNameFromDir(url.getPath().replaceAll("%20", " "), packageName, isRecursion);
			} else if (protocol.equals("jar")) {
				JarFile jarFile = null;
				try{
					jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
				} catch(Exception e){
					throw new RawlayerRuntimeException(e);
				}
				if(jarFile != null){
					classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
				} else {
					logger.warn("JarFile is null when url is : {}", url);
				}
			} else {
				throw new FrameworkRuntimeException("protocol ("+protocol+") is not support!");
			}
		} else {
			/*从所有的jar包中查找包名*/
			URL[] urls = ((URLClassLoader)loader).getURLs();
			logger.warn("Will be scan all jars : {}", Arrays.toString(urls));
			classNames = getClassNameFromJars(urls, packageName, isRecursion);
		}

		return classNames;
	}

	// 独立jar方式启动项目，找正确的路径。待完善！
	public static String getRuntimePath() {
		String classPath = ClassUtil.class.getName().replaceAll("\\.", "/") + ".class";
		URL resource = ClassUtil.class.getClassLoader().getResource(classPath);
		if (resource == null) {
			return null;
		}
		String urlString = resource.toString();
		int insidePathIndex = urlString.indexOf('!');
		boolean isInJar = insidePathIndex > -1;
		if (isInJar) {
			urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
			return urlString;
		}
		return urlString.substring(urlString.indexOf("file:"), urlString.length() - classPath.length());
	}

	/**
	 * 从项目文件获取某包下所有类
	 * @param filePath 文件路径
	 * @param packageName 包名
	 * @param isRecursion 是否遍历子包
	 * @return 类的完整名称
	 */
	private static Set<String> getClassNameFromDir(String filePath, final String packageName, boolean isRecursion) {
		Set<String> classNames = new HashSet<String>();

		File file = new File(filePath);
		File[] files = file.listFiles();
		for (File childFile : files) {
			if (childFile.isDirectory()) {
				if (isRecursion) {
					String newPackageName = null;
					if(packageName.length()==0) newPackageName = childFile.getName();
					else newPackageName = packageName+"."+childFile.getName();
					classNames.addAll(getClassNameFromDir(childFile.getPath(), newPackageName, isRecursion));
				}
			} else if(childFile.isFile()) {
				String fileName = childFile.getName();
				if (fileName.endsWith(".class") && !fileName.contains("$")) {
					String className = fileName.replace(".class", "");
					if(packageName.length()>0) className = packageName+ "." + className;
					classNames.add((className));
				}
			} else {
				logger.error("File : [{}] can not deal!", childFile.getAbsolutePath());
			}
		}

		return classNames;
	}


	/**
	 * @param jarEntries
	 * @param packageName
	 * @param isRecursion
	 * @return
	 */
	private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion){
		Set<String> classNames = new HashSet<String>();

		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			if(!jarEntry.isDirectory()){
				String jarEntryName = jarEntry.getName();
				if(!jarEntryName.endsWith(".class")) continue;
				String className = jarEntryName.replace("/", ".");
				if (className.endsWith(".class") && !className.contains("$") && className.startsWith(packageName)) {
					className = className.replace(".class", StringUtil.EMPTY);
					if(isRecursion) {
						classNames.add((className));
					} else {
						//} else if(!className.replace(packageName+".", "").contains(".")){
						// 这个判断逻辑有问题
						logger.warn("[ FIX? ] className={}", className);
						String tmpClassName = className.replace(packageName+".", StringUtil.EMPTY);
						if(!tmpClassName.contains("."))
							classNames.add((className));
					}
				}
			}
		}

		return classNames;
	}

	/**
	 * 从所有jar中搜索该包，并获取该包下所有类
	 * @param urls URL集合
	 * @param packageName 包路径
	 * @param isRecursion 是否遍历子包
	 * @return 类的完整名称
	 */
	private static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
		Set<String> classNames = new HashSet<String>();

		for (URL url : urls) {
			final String classPath = url.getPath();

			//不必处理非jar文件（比如classes文件夹）
			if (!classPath.endsWith(".jar")) continue;

			JarFile jarFile = null;
			try {
				jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
			} catch (IOException e) {
				throw new RawlayerRuntimeException(e);
			}

			if (jarFile != null) {
				classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
			} else {
				logger.warn("JarFile is null when url.path is : {}", classPath);
			}
		}

		return classNames;
	}

	/**
	 * 对内部类的处理，与JDK的 {@link Class#getSimpleName()} 不一样。
	 * 例如： WorkShop 类里面定义的一个内部类 Person，调用 Person 对象的 {@link Class#getName()} 方法，全类名为：
	 * packagename.WorkShop$Person。
	 * 1）{@link Class#getSimpleName()} 返回 $ 后面的名字 ：Person
	 * 2）本方法返回带有外部类的名字： WorkShop.Person
	 *
	 * @param cls class
	 * @return 短类名
	 */
	public static String getShortClassName(final Class<?> cls) {
		if (cls==null) return StringUtil.EMPTY;
		return getShortClassName(cls.getName());
	}
	public static String getShortClassName(String className) {
		if (className==null) return StringUtil.EMPTY;

		final StringBuilder arrayPrefix = new StringBuilder();

		// Handle array encoding
		if (className.startsWith("[")) {
			while (className.charAt(0) == '[') {
				className = className.substring(1);
				arrayPrefix.append("[]");
			}
			// Strip Object type encoding
			if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
				className = className.substring(1, className.length() - 1);
			}

			if (reverseAbbreviationMap.containsKey(className)) {
				className = reverseAbbreviationMap.get(className);
			}
		}

		final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
		final int innerIdx = className.indexOf(
				INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
		String out = className.substring(lastDotIdx + 1);
		if (innerIdx != -1) {
			out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		}
		return out + arrayPrefix;
	}

	/**
	 * 根据名字，获得 field 对象（包括父类）
	 * @param clazz Class
	 * @param fieldName 属性字段名
	 * @return Field null if not found
	 */
	public static Field getFieldThroughSuper(final Class<?> clazz, final String fieldName) {
		Class<?> currentClass = clazz;
		while (currentClass != null) {
			try {
				return currentClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				currentClass = currentClass.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * 根据属性，获得 field 对象，包括父类的
	 * @param clazz Class
	 * @param fieldName 属性字段名
	 * @return Field null if not found
	 */
	public static Field getFieldThroughSuper(final Class<?> clazz, final PropertyDescriptor prop) {
		if(Class.class == prop.getPropertyType()) return null;
		return getFieldThroughSuper(clazz, prop.getName());
	}

	/**
	 * 得到一个类的所有属性字段对应的 Field 对象列表。
	 *
	 * 因为属性字段来自于 get/set 方法（与成员变量没有任何关系），所以属性字段属于那个类，就是 get/set 方法的归属类。
	 * Field字段是一个类里面真实存在的成员变量，与属性字段没有任何关系。
	 * 情况1：有 getUsername 方法，其对应的属性名就是 username，但是该方法内部可以返回不同名的类成员变量，这时就出问题了。因为无法根据属性名找到对应的field，目前无法处理这种情况
	 * 情况2：当前属性字段，对应的同名field在父类里面，这也是不合法的
	 *
	 * @param clazz Class
	 * @return List<Field>
	 */
	public static List<Field> getPropertyDescriptorFields(final Class<?> clazz) {
		PropertyDescriptor[] props = propertyDescriptors(clazz);
		final List<Field> allFields = new ArrayList<>(props.length-1);
		for (int i = 0; i < props.length; i++) {
			PropertyDescriptor prop = props[i];
			Class<?> propType = prop.getPropertyType(); // 属性字段的数据类型。是由 get 方法决定的。如果set方法的入参类型与get不一样，则这个属性的set方法会被忽略
			if(Class.class.isAssignableFrom(propType)) continue;

			// 1. 获取该属性的归属类
			Method w = prop.getWriteMethod();
			Method r = prop.getReadMethod();
			Method method;
			if(r!=null&&w!=null) method = r;
			else method = (r==null)?w:r;
			Class<?> propClass  = method.getDeclaringClass(); // 属性字段归属的类

			// 2. 在属性的归属类中，找同名的 field
			try {
				Field propField = propClass.getDeclaredField(prop.getName());
				if(propField.getType() != propType) { // 属性和对应的field的数据类型必须一致。 不能用 isAssignableFrom，因为他会判断父类，而我们需要明确判断完全一致。
					logger.debug("Property's Type Not Match Field's Type! PropertyDescriptor=( {} ) Field=( {} )", prop, propField);
					continue;
				}
				allFields.add(propField);
			} catch (NoSuchFieldException e) {
				logger.debug("Property Not Match Field! PropertyDescriptor=( {} ) can not found 'The Same Name' of field in ({}) Or its superclass !", prop, clazz.getName());
			}
		}
		return allFields;
	}

	/**
	 * 得到一个类的所有成员变量，并且包含父类的所有成员变量。
	 * 如果该类与父类存在同名成员变量，父类变量也一起返回
	 * @param clazz Class
	 * @return List<Field>
	 */
	public static List<Field> getAllFields(final Class<?> clazz) {
		return getAllFields(clazz, false);
	}
	/**
	 * 得到一个类的所有成员变量，并且包含父类的所有成员变量。
	 * 如果该类与父类存在同名成员变量，父类变量也一起返回
	 * @param clazz Class
	 * @param autoSetAccessible 自动把每个field设置成可被反射访问
	 * @return List<Field>
	 */
	public static List<Field> getAllFields(final Class<?> clazz, final boolean forceAccess) {
		final List<Field> allFields = new ArrayList<>();
		Class<?> currentClass = clazz;
		while (currentClass != null&&Object.class != currentClass) {
			final Field[] declaredFields = currentClass.getDeclaredFields();
			if(forceAccess) AccessibleObject.setAccessible(declaredFields, true);
			Collections.addAll(allFields, declaredFields);
			currentClass = currentClass.getSuperclass();
		}
		return allFields;
	}

	/**
	 * 得到一个类的所有成员变量，并且包含父类中可被继承的所有成员变量。
	 * 如果该类与父类存在同名成员变量，父类变量被忽略
	 * @param clazz Class
	 * @return List<Field>
	 */
	public static List<Field> getAllVisibleFields(final Class<?> clazz) {
		return getAllVisibleFields(clazz, false);
	}
	/**
	 * 得到一个类的所有成员变量，并且包含父类中可被继承的所有成员变量。
	 * 如果该类与父类存在同名成员变量，父类变量被忽略
	 * @param clazz Class
	 * @param autoSetAccessible 自动把每个field设置成可被反射访问
	 * @return List<Field>
	 */
	public static List<Field> getAllVisibleFields(final Class<?> clazz, final boolean forceAccess) {
		final Field[] thisClassFields = clazz.getDeclaredFields();
		final List<Field> allFields = new ArrayList<>();
		Collections.addAll(allFields, thisClassFields);

		// 记录当前类每个 field 的名字，用于下面从父类中剔除相同名字的 field
		Set<String> currentFieldNames = new HashSet<>(thisClassFields.length);
		for (int i = 0; i < thisClassFields.length; i++) {
			currentFieldNames.add(thisClassFields[i].getName());
		}

		Class<?> currentClass = clazz.getSuperclass();
		while (currentClass != null&&Object.class != currentClass) {
			final Field[] declaredFields = currentClass.getDeclaredFields();
			for (int i = 0; i < declaredFields.length; i++) { // TODO 需要判断 !f.getName().contains("$") ???
				Field field = declaredFields[i];
				if(Modifier.isPrivate(field.getModifiers()))
					continue; // 排除父类中的私有字段，因为这些字段对子类是不可见的
				if(!currentFieldNames.contains(field.getName())) {// 父类中存在同名字段则跳过
					if(forceAccess) field.setAccessible(true);
					allFields.add(field);
					currentFieldNames.add(field.getName());
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		currentFieldNames.clear();
		return allFields;
	}

	/**
	 * 得到一个类的所有指定类型的方法，包含其各级父类的方法。
	 * 注意：如果程序中使用了 lambda 表达式，会被getDeclaredMethods得到！！！
	 * @param clazz Class
	 * @param type int 获取方法的类型，需使用 Modifier 类里面的静态类型。
	 *                比如：Modifier.PUBLIC 、(Modifier.PUBLIC \ Modifier.STATIC) 等。多种类型使用 '\' 或到一起
	 * @param  includeStatic boolean 是否包括static类型的方法
	 * @return Set<Method>
	 */
	public static Set<Method> getAllMethods(final Class<?> clazz, int type, boolean includeStatic) {
		Set<Method> methodList = new HashSet<>() ;
		Class<?> tempClass = clazz;
		while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
			//Method[] curMethods = tempClass.getMethods(); 这个方法会把父类中的 public方法都取过来，包括 wait、toString等
			Method[] curMethods = tempClass.getDeclaredMethods();
			for(Method method : curMethods) {
//				if(Modifier.isStatic(method.getModifiers())) continue; // 不包括static方法
				if( (method.getModifiers() & type) > 0 ) { // 这个方法是预期的类型
					if (!includeStatic && Modifier.isStatic(method.getModifiers())) continue;
					methodList.add(method);
				}
			}
			tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
			if(Object.class.isAssignableFrom(tempClass)) {
				if(tempClass.getSuperclass()==null) break;
			}
		}
		return methodList;
	}
}
