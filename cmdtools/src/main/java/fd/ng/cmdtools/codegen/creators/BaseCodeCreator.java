package fd.ng.cmdtools.codegen.creators;

import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BaseCodeCreator {
	// 模版文件根目录（templateFileRootPath）下的分类目录名
	public static final String TEMPLATE_TYPE_ROOTDIR = "/";
	public static final String TEMPLATE_TYPE_MAIN_JAVA = "mainjava";
	public static final String TEMPLATE_TYPE_MAIN_RESOURCES = "mainresources";
	public static final String TEMPLATE_TYPE_TEST_JAVA = "testjava";
	public static final String TEMPLATE_TYPE_TEST_RESOURCES = "testresources";
	// 生成的代码文件的目录结构
	static final String SrcMainJavaFolder = "src/main/java/";
	static final String SrcMainResourceFolder = "src/main/resources/";
	static final String SrcTestJavaFolder = "src/test/java/";
	static final String SrcTestResourceFolder = "src/test/resources/";

	public final String prjBasePackageName; // 新工程的根包名
	public final String prjRootPathForGenCodefile; // 为新工程生成源文件的根目录
	public final String templateFileRootPath; // 模版文件的根目录 该目录下的第一级目录分别是：mainjava, mainresources, testjava, testresources
	public final String prjSrcMainJavaPath; // java main 源文件的根目录
	public final String prjSrcMainResourcePath; // java main 源文件的根目录
	public final String prjSrcTestJavaPath; // java test 源文件的根目录
	public final String prjSrcTestResourcePath; // java test 源文件的根目录

	private static final Configuration fmConfiguration = new Configuration(Configuration.VERSION_2_3_28);

	public BaseCodeCreator(String prjGeneratedCodefileRootPath, String prjBasePackageName, String templateFileRootPath) {
		this.prjBasePackageName = prjBasePackageName;

		prjGeneratedCodefileRootPath = prjGeneratedCodefileRootPath.replace("\\", "/");
		if(prjGeneratedCodefileRootPath.endsWith("/"))
			this.prjRootPathForGenCodefile = prjGeneratedCodefileRootPath;
		else
			this.prjRootPathForGenCodefile = prjGeneratedCodefileRootPath + "/";
		templateFileRootPath = templateFileRootPath.replace("\\", "/");
		if(templateFileRootPath.endsWith("/"))
			this.templateFileRootPath = templateFileRootPath;
		else
			this.templateFileRootPath = templateFileRootPath + "/";

		String basePkgPath = this.prjBasePackageName.replace(".", "/") + "/";
		this.prjSrcMainJavaPath = this.prjRootPathForGenCodefile + SrcMainJavaFolder + basePkgPath;
		this.prjSrcMainResourcePath = this.prjRootPathForGenCodefile + SrcMainResourceFolder;
		this.prjSrcTestJavaPath = this.prjRootPathForGenCodefile + SrcTestJavaFolder + basePkgPath;
		this.prjSrcTestResourcePath = this.prjRootPathForGenCodefile + SrcTestResourceFolder;

		// 模版文件被打包到jar里面，使用 classloader 装载，则使用本函数
//		fmConfiguration.setClassForTemplateLoading(this.getClass(), "/" + FtlTemplateBasePackage.replace(".", "/"));
		try {
			fmConfiguration.setDirectoryForTemplateLoading(new File(this.templateFileRootPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		fmConfiguration.setDefaultEncoding(CodecUtil.UTF8_STRING);
	}

	/**
	 * 删除整个代码目录，并重新创建各基本目录
	 */
	public void clearAndCreateProjectDirs() {
		try {
			// 清理掉所有上次创建的文件
			Path prjSrcRootPath = Paths.get(this.prjRootPathForGenCodefile+"src");
			if(Files.isDirectory(prjSrcRootPath)) {
				infoOutln("clean path : " + prjSrcRootPath.toString());
				Files.walk(prjSrcRootPath).filter(Files::isRegularFile).map(Path::toFile) // 目录文件都删除： .sorted(Comparator.reverseOrder()).map(Path::toFile)
//						.forEach(System.out::println);
						.forEach(file -> {
							if(!file.delete()) errorOutln("delete fail! filename="+file.toString());
						});
			}
			// 建立工程源码根目录
			Files.createDirectories( Paths.get(this.prjSrcMainJavaPath) );
			Files.createDirectories( Paths.get(this.prjSrcMainResourcePath) );
			Files.createDirectories( Paths.get(this.prjSrcTestJavaPath) );
			Files.createDirectories( Paths.get(this.prjSrcTestResourcePath) );
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getSrcRootPath() {
		return this.prjRootPathForGenCodefile;
	}
	public String getJavaSrcFileDir() {
		return this.prjSrcMainJavaPath;
	}
	public String getJavaSrcFileDir(String subPackageName) {
		return prjSrcMainJavaPath + subPackageName.replace(".", "/");
	}

	void genOneCodeFile(final String ftlTypePkgName, Map<String, Object> ftlArgsValueMap) {
		genOneCodeFile(ftlTypePkgName, ftlArgsValueMap, null);
	}

	/**
	 *
	 * @param ftlTypePkgName 要处理的模版文件所在分类包名（也即template下的第一级子包名）：mainjava, mainresources, testjava, testresources
	 * @param ftlArgsValueMap freemaker需要的用于替换模版文件中变量的填充数据
	 * @param specifiedFtlFileName 明确指定要生成哪个源文件。这个名字是template下的ftl文件
	 */
	void genOneCodeFile(final String ftlTypePkgName, Map<String, Object> ftlArgsValueMap, final String specifiedFtlFileName) {
		final String subPackageName = (String)ftlArgsValueMap.get("subPackage");
		// 真正要生成的目标代码文件的所在目录
		final String curSubPkgPathName = getCodefileDirName(ftlTypePkgName, subPackageName);

		Set<String> putkeyInThis = new HashSet<>(); // 存储记录在这个函数内部向 ftlArgsValueMap 中新增的key，用于在最后清理掉
		Writer out = null;
		try {
			// 得到当前子包下所有ftl文件
			Set<String> ftlFileNames = getFtlFileNames(ftlTypePkgName, subPackageName, specifiedFtlFileName);
			// ---------------  开始创建目标代码文件
			for(final String ftlFileName : ftlFileNames) {
				// 加载模版文件。注意：如果文件中存在'${...}'，需要转义为 ${r'${...}'}。因为'${...}'是FreeMaker的变量定义符号
				String ftlPathname = ftlTypePkgName + "/" +subPackageName.replace(".", "/") + "/" + ftlFileName;
				if(TEMPLATE_TYPE_ROOTDIR.equals(ftlTypePkgName)) ftlPathname = ftlFileName;
				Template template = fmConfiguration.getTemplate(ftlPathname);

				// 真正要生成的目标代码文件
				// 如果外面没有明确传入要生成的目标代码文件名，则使用模版文件(去掉ftl后缀)作为目标文件名
				final String destCodefileName = (String)ftlArgsValueMap.getOrDefault("destCodefileName", ftlFileName.replaceAll(".ftl$", ""));
				File srcFile = new File(curSubPkgPathName + destCodefileName);

				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(srcFile), CodecUtil.UTF8_CHARSET));
				if(TEMPLATE_TYPE_MAIN_JAVA.equals(ftlTypePkgName)||TEMPLATE_TYPE_TEST_JAVA.equals(ftlTypePkgName)) {
					String className = (String)ftlArgsValueMap.get("className");
					if(className==null) { // 如果外面没有明确传入类名，则使用目标文件名（所以，模版文件必须是.java.ftl后缀）
						if(!destCodefileName.endsWith(".java")) throw new RuntimeException("template file ["+ftlFileName+"] must be '.java.ftl' suffix!");
						ftlArgsValueMap.put("className", destCodefileName.replaceAll(".java$", ""));
						putkeyInThis.add("className"); // 因为新增了key=className，所以要记录下来，最后清理掉
					}
				}
				template.process(ftlArgsValueMap, out);
				out.close();
				putkeyInThis.forEach(ftlArgsValueMap::remove);
				infoOutln(String.format("Gen : %-30s [ path=%s ]", destCodefileName, curSubPkgPathName));
			}
			if(ftlFileNames.isEmpty())
				warnOutln(String.format("Gen : %-30s [ path=%s ]", "Nothing!", curSubPkgPathName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try{if(out!=null) out.close();}catch (Exception e){}
		}
	}

	// 获取指定子包目录下的所有ftl文件（不包括子目录）
	private Set<String> getFtlFileNames(String ftlTypePkgName, String subPackageName, String specifiedFtlFileName) {
		// ftl模版文件所在的根路径
		String ftlPackagePath = this.templateFileRootPath  // 模版文件存放的根目录。每个模版文件在这个目录下用自己的子包建立目录
				+ ftlTypePkgName + "/" + subPackageName.replace(".", "/");
		if(TEMPLATE_TYPE_ROOTDIR.equals(ftlTypePkgName)) ftlPackagePath = this.templateFileRootPath;
		Path ftlPath = Paths.get(ftlPackagePath);
		if(!ftlPath.toFile().isDirectory()) return Collections.emptySet();

		if(StringUtil.isNotEmpty(specifiedFtlFileName)) {
			Set<String> ftlFileNames = new HashSet<>(1);
			ftlFileNames.add(specifiedFtlFileName);
			return ftlFileNames;
		}
		try {
			Set<String> ftlFileNames = new HashSet<>();
			Files.list(ftlPath).filter(Files::isRegularFile).forEach(path->{
				File file = path.toFile();
				if(file.getName().endsWith(".ftl"))
					ftlFileNames.add(file.getName());
			});
			return ftlFileNames;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 要创建的目标代码文件的根目录
	public String getCodefileRootDirName(String ftlTypePkgName) {
		if(TEMPLATE_TYPE_ROOTDIR.equals(ftlTypePkgName))
			return this.prjRootPathForGenCodefile;
		else if(TEMPLATE_TYPE_MAIN_JAVA.equals(ftlTypePkgName))
			return this.prjSrcMainJavaPath;
		else if(TEMPLATE_TYPE_MAIN_RESOURCES.equals(ftlTypePkgName))
			return this.prjSrcMainResourcePath;
		else if(TEMPLATE_TYPE_TEST_JAVA.equals(ftlTypePkgName))
			return this.prjSrcTestJavaPath;
		else if(TEMPLATE_TYPE_TEST_RESOURCES.equals(ftlTypePkgName))
			return this.prjSrcTestResourcePath;
		else {
			throw new RuntimeException("unsupport template type : '"+ftlTypePkgName+"'");
		}
	}
	// 真正要生成的目标代码文件的所在目录
	public String getCodefileDirName(String ftlTypePkgName, String subPackageName) {
		String curSubPkgPathName = getCodefileRootDirName(ftlTypePkgName) + subPackageName.replace(".", "/");
		if(!curSubPkgPathName.endsWith("/")) curSubPkgPathName += "/"; // subPackageName为""时不需要追加
		return curSubPkgPathName;
	}
	public void cleanCodeFiles(String ftlTypePkgName, String subPackageName) {
		// 删除该子包目录下的所有文件
		String curSubPkgPathName = getCodefileDirName(ftlTypePkgName, subPackageName);
		if(Paths.get(curSubPkgPathName).toFile().isDirectory()) { // 首次执行时，该目录还没有被创建过，不需要执行下面的清理工作
			if (subPackageName.length() < 1)
				FileUtil.deleteDirectoryFiles(curSubPkgPathName, 1, "*"); // 没有指定子包名，应该只删除根目录下的文件，不要把根下的其他子包目录中的文件删掉
			else {
				FileUtil.deleteDirectoryFiles(curSubPkgPathName);
			}
		} else {
			try {
				Files.createDirectories( Paths.get(curSubPkgPathName) );
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

//	public void assertTemplateTypePkgName(String ftlTypePkgName) {
//		if(	!(TEMPLATE_TYPE_MAIN_JAVA.equals(ftlTypePkgName)
//				|| TEMPLATE_TYPE_MAIN_RESOURCES.equals(ftlTypePkgName)
//				|| TEMPLATE_TYPE_TEST_JAVA.equals(ftlTypePkgName)
//				|| TEMPLATE_TYPE_TEST_RESOURCES.equals(ftlTypePkgName))
//		)
//		{
//			throw new RuntimeException("unsupport template type : '"+ftlTypePkgName+"'");
//		}
//	}

	protected String upperFirstCharHump(String name) {
		//把两个下划线替换成一个
		while (name.contains("__")) {
			name = name.replace("__", "_");
		}
		//下划线开头或结尾，则去掉
		if(name.charAt(0)=='_') name = name.substring(1);
		if(name.charAt(name.length()-1)=='_') name = name.substring(0, name.length()-1);
		//下划线转驼峰
		name = StringUtil.underlineToHump(name);
		//首字母大写
		return upperFirstChar(name);
	}
	/**
	 * 返回首字母大写
	 * @param word
	 * @return
	 */
	protected String upperFirstChar(String word) {
		return word.substring(0,1).toUpperCase().concat(word.substring(1));
	}
	/**
	 * 返回首字母小写
	 * @param word
	 * @return
	 */
	protected String lowerFirstChar(String word) {
		return word.substring(0,1).toLowerCase().concat(word.substring(1));
	}

	public void errorOutln(String msg) {
		System.out.printf("[ERROR] %s%n", msg);
	}
	public void warnOutln(String msg) {
		System.out.printf("[WARN ] %s%n", msg);
	}
	public void infoOutln() {
		System.out.println();
	}
	public void infoOutln(String msg) {
		System.out.printf("[INFO ] %s%n", msg);
	}
}
