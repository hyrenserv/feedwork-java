package fd.ng.cmdtools.codegen.creators;

import fd.ng.core.utils.ClassUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.meta.ColumnMeta;
import fd.ng.db.meta.MetaOperator;
import fd.ng.db.meta.TableMeta;
import fd.ng.db.util.TypeMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// 这是使用java代码去生成实体类，已经用 FreeMaker 代替了。 这里仅保留这些代码，或许以后能用上，但不需要使用了
@Deprecated
public class TableEntityCreator extends BaseCodeCreator {
	private static final String entitySubPackageName = "entity.inner"; // 文件生成到的子包

	public TableEntityCreator(String prjGeneratedCodefileRootPath, String prjBasePackageName, String templateFileRootPath) {
		super(prjGeneratedCodefileRootPath, prjBasePackageName, templateFileRootPath);
	}
	/**
	 * 以基本包名为基础，对<b>指定DB</b>的表信息自动生成所有的实体类
	 * 被生成的实体类中，所有属性都是对象，不存在int等主类型，方便后续对null值的操作
	 *
	 * @param dbName dbinfo.conf中的databases.name
	 */
	public void makeupCodeFileByThisCode(String dbName) {
		DatabaseWrapper db = null;
		try {
			Path codeRootPath = Paths.get(getJavaSrcFileDir(entitySubPackageName));
			if(Files.isDirectory(codeRootPath))
				Files.walk(codeRootPath).sorted(Comparator.reverseOrder()).map(Path::toFile)
						//.peek(System.out::println)
						.forEach(File::delete);
			Files.createDirectories(codeRootPath);
			db = new DatabaseWrapper.Builder().dbname(dbName).create();
			List<TableMeta> tables = MetaOperator.getTablesWithColumns(db, null);
			for(TableMeta tableMeta : tables) {
				String javaCode = genCodePackage() +
						genCodeImport() +
						genCodeClassLine(tableMeta) +
						genCodeFields(tableMeta) +
						genCodeStatic(tableMeta) +
						genCodeConstructor(tableMeta) +
						genCodeGetSetMethods(tableMeta) +
						"}\n";
				createOrReplaceJavaFilename(tableMeta, javaCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(db!=null) db.close();
		}
	}

	/**
	 * 为一张表，生成 java 代码文件
	 * @param tableMeta
	 * @param javaCode
	 */
	private void createOrReplaceJavaFilename(TableMeta tableMeta, String javaCode) {
		String javaFilename = getJavaSrcFileDir(entitySubPackageName) + FileUtil.PATH_SEPARATOR +
				getJavaClassName(tableMeta) + ".java";
		boolean ok = FileUtil.createOrReplaceFile(javaFilename, javaCode);
		if(ok) System.out.println("created file : " + javaFilename);
	}

	// ---------------------------------- 以下 6 个genCode函数，依次生成一个Java代码文件的各个程序代码块： package/import/public class/static/属性/getset方法
	private String genCodePackage() {
		return "package " + prjBasePackageName + ClassUtil.PACKAGE_SEPARATOR + entitySubPackageName + ";\n";
	}
	private String genCodeImport() {
		return 	"\nimport fd.ng.db.entity.TableEntity;\n" +
				"\n" +
				"import java.math.BigDecimal;\n" +
				"import java.util.*;\n" +
				"import java.time.Instant;\n" +
				"import java.time.LocalDate;\n" +
				"import java.time.LocalTime;\n" +
				"import java.time.ZonedDateTime;\n" +
				"import fd.ng.db.entity.anno.Column;\n" +
				"import fd.ng.db.entity.anno.Table;\n";
	}
	private String genCodeClassLine(TableMeta tableMeta) {
		Map<String, String> map = getEntityClassAndTableAnno(tableMeta);
		String AnnoTableName = Optional.of(map.get("AnnoTableName")).get();
		String EntityClassName = getJavaClassName(tableMeta);
		return "\n@Table(tableName = \""+AnnoTableName+"\" )\n" +
				"public class "+EntityClassName+" extends TableEntity {\n";
	}
	private String genCodeStatic(TableMeta tableMeta) {
		// 初始化主键名清单
		Set<String> pks = tableMeta.getPrimaryKeys();
		final StringBuilder code = new StringBuilder();
		pks.forEach((pkname)->{
			code.append("\t\ttmpPrimaryKeys.add(\"").append(pkname).append("\");\n");
		});
		return "\n" +
				"\tstatic {\n" +
				"\t\tSet<String> tmpPrimaryKeys = new HashSet<>();\n" +
				code.toString() +
				"\t\t__PrimaryKeys = Collections.unmodifiableSet(tmpPrimaryKeys);\n" +
				"\t}";

//		Map<String, ColumnMeta> columnMetas = tableMeta.getColumnMetas();
//		final StringBuilder code = new StringBuilder();
//		columnMetas.forEach((colName, colMeta)->{
//			// key : 字段名, value : 字段类型
//			code.append("\t\ttmpAllFieldTypes.put(\"").append(genFieldName(colMeta)).append("\"")
//				.append(", ").append(genFieldTypeName(colMeta)).append(".class);\n");
//		});
//		return  "\t// Map.key=成员变量名，Map.value=成员变量的Java类型\n" +
//				"\tpublic static final Map<String, Class<?>> __AllFieldProps__;\n" +
//				"\tstatic {\n" +
//				"\t\tMap<String, Class<?>> tmpAllFieldTypes = new HashMap<>();\n" +
//				code.toString() +
//				"\t\t__AllFieldProps__ = Collections.unmodifiableMap(tmpAllFieldTypes);\n" +
//				"\t}\n";
	}
	private String genCodeFields(TableMeta tableMeta) {
		Map<String, ColumnMeta> columnMetas = tableMeta.getColumnMetas();
		final StringBuilder code = new StringBuilder("\n");
		columnMetas.forEach((colName, colMeta)->{
			// key : 字段名, value : 字段类型
			code.append("\tprivate ").append(genFieldTypeName(colMeta)).append(" ")
				.append(genFieldName(colMeta)).append(";\n");
		});
		return code.toString();
	}
	private String genCodeConstructor(TableMeta tableMeta) {
		final StringBuilder code = new StringBuilder("\n");
		code.append("\tpublic ").append(getJavaClassName(tableMeta)).append("() {\n");

//		// 添加主键清单
//		Set<String> pks = tableMeta.getPrimaryKeys();
//		pks.forEach((pkname)->{
//			code.append("\t\taddPKName(\"").append(pkname).append("\");\n");
//		});

		code.append("\t}\n");
		return code.toString();
	}
	private String genCodeGetSetMethods(TableMeta tableMeta) {
		Map<String, ColumnMeta> columnMetas = tableMeta.getColumnMetas();
		final StringBuilder code = new StringBuilder("\n");
		columnMetas.forEach((colName, colMeta)->{
			String fieldName = genFieldName(colMeta);
			String UpperedFieldName = upperFirstChar(fieldName);
			String fieldType = genFieldTypeName(colMeta);
			String getMethod = "\tpublic " + fieldType +" get"+UpperedFieldName+"() {\n" +
					"\t\treturn "+fieldName+";\n" +
					"\t}\n";
			String setMethod = "\tpublic void set" + UpperedFieldName + "(" + fieldType + " "+fieldName+") {\n";
			// 主键字段不能设置为控制字段
			Set<String> pks = tableMeta.getPrimaryKeys();
			if(!pks.contains(fieldName))
				setMethod += "\t\tif("+fieldName+"==null) addNullValueField(\""+fieldName+"\");\n";
			setMethod +=
					"\t\tthis."+fieldName+" = "+fieldName+";\n" +
					"\t}\n";
			code.append(getMethod).append(setMethod);
		});
		return code.toString();
	}
	/**
	 * 根据表名，得到要生成的实体类名字（也就是Java Class文件名）
	 * @param tableMeta TableMeta
	 * @return 两行数据的Map，（EntityClassName -> Java类名）；（AnnoTableName -> 数据库的原始表名）
	 */
	private Map<String, String> getEntityClassAndTableAnno(TableMeta tableMeta) {
		String tableName = tableMeta.getTableName();
		String EntityClassName = upperFirstCharHump(tableName);
		Map<String, String> map = new HashMap<>(2);
		map.put("EntityClassName", EntityClassName);
		map.put("AnnoTableName", tableName);
		return map;
	}
	private String getJavaClassName(TableMeta tableMeta) {
		return Optional.of(getEntityClassAndTableAnno(tableMeta).get("EntityClassName")).get();
	}

	/**
	 * 用表字段名生成java属性名
	 * @param
	 * @return
	 */
//	private String genFieldName(String columnName) {
//		return columnName.toLowerCase();  // 不做驼峰转换，使用原始名字
//	}
	private String genFieldName(ColumnMeta columnMeta) {
		// 不做驼峰转换，使用原始名字，但是必须小写。因为JavaBean属性名要求：前两个字母要么都大写，要么都小写
		return columnMeta.getName().toLowerCase();
	}
	/**
	 * 得到对应的java数据类型
	 * @return
	 */
	private String genFieldTypeName(ColumnMeta columnMeta) {
		return TypeMapper.getJavaType(columnMeta.getTypeOfSQL()).getSimpleName();
	}
}
