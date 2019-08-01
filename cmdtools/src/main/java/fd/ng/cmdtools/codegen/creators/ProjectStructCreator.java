package fd.ng.cmdtools.codegen.creators;

import fd.ng.core.utils.Validator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.meta.ColumnMeta;
import fd.ng.db.meta.MetaOperator;
import fd.ng.db.meta.TableMeta;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.*;

/**
 * 代码生成的主处理类。
 * 每个 gen 开头的函数用于生成一类代码文件。
 * 如果有子定义的代码生成诉求，有两种处理方式：
 * 1）把模版文件放到 template 下初始存在的任意目录下，即可被生成
 * 2）使用自己定义的新包，那么可自己编写程序调用 genCustomCodeFiles 函数来生成文件
 * 3）如果是自定义实体，可把模版文件放到 entity 下面（entity.ftl这个名字不能使用）
 */
public class ProjectStructCreator extends BaseCodeCreator {
	private String _SysParaTableName = "sys_para";
	private String _TellersTableName = "tellers";
	public ProjectStructCreator(String prjGeneratedCodefileRootPath, String prjBasePackageName, String templateFileRootPath) {
		super(prjGeneratedCodefileRootPath, prjBasePackageName, templateFileRootPath);
	}

	public String getSysParaTableName() {
		return _SysParaTableName;
	}

	public void setSysParaTableName(String SysParaTableName) {
		this._SysParaTableName = SysParaTableName;
	}

	public String getTellersTableName() {
		return _TellersTableName;
	}

	public void setTellersTableName(String TellersTableName) {
		this._TellersTableName = TellersTableName;
	}

	/**
	 * 建立java项目基本结构，生成通用的代码
	 * @param customSubPkgNameList 用户可以自己传入子包名
	 * @param customData 用户可以自己传入模版里使用的数据
	 */
	public void genGeneralJavaCodeFiles(List<String> customSubPkgNameList, Map<String, Object> customData) {
		infoOutln("==================== Generate Java Files ====================");

		List<String> subPackageNames = new ArrayList<>();
		subPackageNames.add("biz.zbase");
		subPackageNames.add("biz.zauth");
		subPackageNames.add("biz.zsys");
		subPackageNames.add("biz.ztest");
		subPackageNames.add("exception");
		subPackageNames.add("main");
		subPackageNames.add("util");
		if(!customSubPkgNameList.isEmpty()) subPackageNames.addAll(customSubPkgNameList);

		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		for(String subPkgName : subPackageNames) {
			ftlArgsValueMap.put("subPackage", subPkgName);
			cleanCodeFiles(TEMPLATE_TYPE_MAIN_JAVA, subPkgName);
//			cleanCodeFiles(TEMPLATE_TYPE_TEST_JAVA, subPkgName);
			infoOutln(String.format("====> basePackage=%s | subPackage=%-15s", prjBasePackageName, subPkgName));
			genOneCodeFile(TEMPLATE_TYPE_MAIN_JAVA, ftlArgsValueMap);
//			genOneCodeFile(TEMPLATE_TYPE_TEST_JAVA, ftlArgsValueMap);
		}
	}
	public void genGeneralJavaCodeFiles() {
		genGeneralJavaCodeFiles(Collections.emptyList(), Collections.emptyMap());
	}

	public void genGeneralJavaTestCodeFiles(List<String> customSubPkgNameList, Map<String, Object> customData) {
		infoOutln("==================== Generate Java Testcase Files ====================");

		List<String> subPackageNames = new ArrayList<>();
		subPackageNames.add("biz.zsys");
		subPackageNames.add("biz.ztest");
		subPackageNames.add("exception");
		subPackageNames.add("extest.parallel");
		subPackageNames.add("testbase");
		subPackageNames.add("util");
		if(!customSubPkgNameList.isEmpty()) subPackageNames.addAll(customSubPkgNameList);

		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		for(String subPkgName : subPackageNames) {
			ftlArgsValueMap.put("subPackage", subPkgName);
			cleanCodeFiles(TEMPLATE_TYPE_TEST_JAVA, subPkgName);
			infoOutln(String.format("====> basePackage=%s | subPackage=%-15s", prjBasePackageName, subPkgName));
			genOneCodeFile(TEMPLATE_TYPE_TEST_JAVA, ftlArgsValueMap);
		}
	}
	public void genGeneralJavaTestCodeFiles() {
		genGeneralJavaTestCodeFiles(Collections.emptyList(), Collections.emptyMap());
	}

	/**
	 * 生成资源文件
	 * @param customSubPkgNameList 用户可以自己传入子包名
	 * @param customData 用户可以自己传入模版里使用的数据
	 */
	public void genResourceFiles(List<String> customSubPkgNameList, Map<String, Object> customData) {
		infoOutln("==================== Generate Resource Files ====================");

		List<String> subPackageNames = new ArrayList<>();
		subPackageNames.add(""); // 根目录下的模版文件
		subPackageNames.add("fdconfig");
		subPackageNames.add("i18n");
		if(!customSubPkgNameList.isEmpty()) subPackageNames.addAll(customSubPkgNameList);

		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		for(String subPkgName : subPackageNames) {
			ftlArgsValueMap.put("subPackage", subPkgName);
			cleanCodeFiles(TEMPLATE_TYPE_TEST_RESOURCES, subPkgName);
			cleanCodeFiles(TEMPLATE_TYPE_MAIN_RESOURCES, subPkgName);
			infoOutln(String.format("====> basePackage=%s | subPackage=%-15s", prjBasePackageName, subPkgName));
			genOneCodeFile(TEMPLATE_TYPE_TEST_RESOURCES, ftlArgsValueMap);
			genOneCodeFile(TEMPLATE_TYPE_MAIN_RESOURCES, ftlArgsValueMap);
		}
	}
	public void genResourceFiles() {
		genResourceFiles(Collections.emptyList(), Collections.emptyMap());
	}

	// --------------------  生成 build.gradle 等文件 ----------------------

	public void genGradleFiles() {
		genGradleFiles(Collections.emptyMap());
	}
	public void genGradleFiles(Map<String, Object> customData) {
		infoOutln("==================== Generate gradle Files ====================");

		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		ftlArgsValueMap.put("subPackage", "");
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		genOneCodeFile(TEMPLATE_TYPE_ROOTDIR, ftlArgsValueMap);
	}

	// --------------------  生成实体 开始 ----------------------

	/**
	 * 生成生成实体
	 * @param customData 用户可以自己传入模版里使用的数据
	 */
	public void genEntityCodeFiles(String dbName, Map<String, Object> customData) {
		infoOutln("==================== Generate Java Entity Files ====================");

		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		ftlArgsValueMap.put("subPackage", "entity");
		ftlArgsValueMap.put("serialVersionUID", "32"+System.currentTimeMillis());
		cleanCodeFiles(TEMPLATE_TYPE_MAIN_JAVA, "entity");
		infoOutln(String.format("====> basePackage=%s | subPackage=%-15s", prjBasePackageName, "entity"));

		try (DatabaseWrapper db = new DatabaseWrapper.Builder().dbname(dbName).create()) {
			List<TableMeta> tables = MetaOperator.getTablesWithColumns(db, null);
			boolean hasTellersTable = false, hasSys_paramTable = false;
			for(TableMeta tableMeta : tables) {
				String tableName = tableMeta.getTableName();
				String entityClassName = upperFirstCharHump(tableName);
				Set<String> pks = tableMeta.getPrimaryKeys();
				Collection<ColumnMeta> columnMetaList = tableMeta.getColumnMetas().values();

				ftlArgsValueMap.put("tableName", tableName);
				ftlArgsValueMap.put("className", entityClassName);
				ftlArgsValueMap.put("pkNameList", pks);
				ftlArgsValueMap.put("columnMetaList", columnMetaList);
				ftlArgsValueMap.put("destCodefileName", entityClassName+".java");
				genOneCodeFile(TEMPLATE_TYPE_MAIN_JAVA, ftlArgsValueMap, "entity.ftl");
				// 操作员表和系统参数表如果没有，则自动创建
				if(_TellersTableName.equalsIgnoreCase(tableName)) hasTellersTable = true;
				if(_SysParaTableName.equalsIgnoreCase(tableName)) hasSys_paramTable = true;
			}

			// 操作员表和系统参数表如果没有，则自动创建
			if(!hasTellersTable) {
				ftlArgsValueMap.put("tableName", _TellersTableName);
				ftlArgsValueMap.put("className", "Tellers");
				List<String> pks = new ArrayList<>(); pks.add("te_operid");
				ftlArgsValueMap.put("pkNameList", pks);
				ftlArgsValueMap.put("columnMetaList", makeTellersColumnMeta());
				ftlArgsValueMap.put("destCodefileName", "Tellers.java");
				genOneCodeFile(TEMPLATE_TYPE_MAIN_JAVA, ftlArgsValueMap);
			}
			if(!hasSys_paramTable) {
				ftlArgsValueMap.put("tableName", _SysParaTableName);
				ftlArgsValueMap.put("className", "SysPara");
				List<String> pks = new ArrayList<>(); pks.add("para_name");
				ftlArgsValueMap.put("pkNameList", pks);
				ftlArgsValueMap.put("columnMetaList", makeSysParaColumnMeta());
				ftlArgsValueMap.put("destCodefileName", "SysPara.java");
				genOneCodeFile(TEMPLATE_TYPE_MAIN_JAVA, ftlArgsValueMap);
			}
		}
	}
	public void genEntityCodeFiles(String dbName) {
		genEntityCodeFiles(dbName, Collections.emptyMap());
	}

	// --------------------  生成实体 结束 ----------------------

	/**
	 * 使用用户自己编写的模版文件生成代码文件
	 * @param ftlTypePkgName 模版归属分类：BaseCodeCreator中 TEMPLATE_TYPE_MAIN_JAVA 等4个静态变量
	 * @param subPackage 用户自己编写的模版文件所在的子包名。需提供全路径名，比如 xxx.yyy.zzz
	 * @param templateFilename 用户自己编写的模版文件名。如果为null，则对该子包下所有文件生成代码文件
	 * @param customData 这个模版文件里面需要的各种参数数据
	 */
	public void genCustomCodeFiles(String ftlTypePkgName, String subPackageName, @Nullable String templateFilename, Map<String, Object> customData) {
		Validator.notNull(ftlTypePkgName);
		Validator.notNull(subPackageName);
		Validator.notNull(customData);
		Map<String, Object> ftlArgsValueMap = new HashMap<>(3);
		ftlArgsValueMap.put("basePackage", prjBasePackageName);
		if(!customData.isEmpty()) ftlArgsValueMap.putAll(customData);
		ftlArgsValueMap.put("subPackage", subPackageName);
		cleanCodeFiles(ftlTypePkgName, subPackageName);
		infoOutln(String.format("====> basePackage=%s | subPackage=%-15s", prjBasePackageName, subPackageName));
		if(templateFilename==null)
			genOneCodeFile(ftlTypePkgName, ftlArgsValueMap);
		else
			genOneCodeFile(ftlTypePkgName, ftlArgsValueMap, templateFilename);
	}

	// ----------------- 内部使用的公共函数 -----------------

	private List<ColumnMeta> makeTellersColumnMeta() {
		List<ColumnMeta> columnMetaList = new ArrayList<>();
		columnMetaList.add(new ColumnMeta("te_operid", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("te_name", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("te_dept_type", Types.CHAR));
		columnMetaList.add(new ColumnMeta("te_role_id", Types.VARCHAR));
		return columnMetaList;
	}
	private List<ColumnMeta> makeSysParaColumnMeta() {
		List<ColumnMeta> columnMetaList = new ArrayList<>();
		columnMetaList.add(new ColumnMeta("para_name", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("para_value", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("remark", Types.VARCHAR));
		return columnMetaList;
	}
}
