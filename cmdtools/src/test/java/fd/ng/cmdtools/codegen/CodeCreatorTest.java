package fd.ng.cmdtools.codegen;

import fd.ng.cmdtools.CmdMain;
import fd.ng.cmdtools.codegen.creators.BaseCodeCreator;
import fd.ng.cmdtools.codegen.creators.ProjectStructCreator;
import fd.ng.core.utils.FileUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.meta.ColumnMeta;
import fd.ng.test.junit.FdBaseTestCase;
import fd.ng.test.junit.TestCaseLog;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Types;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("代码生成工具的测试需要手工执行并观察")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CodeCreatorTest extends FdBaseTestCase {
	private static final String ftlRootDir = "D:\\java\\app\\intellij2018\\feedwork-java\\cmdtools\\src\\main\\java\\fd\\ng\\cmdtools\\codegen\\template";
//	private static final String testTableName = "__EntityCodeGen_fd_17456";
//	@BeforeClass
//	public static void start() {
//		DatabaseWrapper db = new DatabaseWrapper();
//		if(!db.isExistTable(testTableName))
//			db.ExecDDL("create table " + testTableName + "(" +
//					"  name varchar(20) primary key" +
//					", age int not null" +
//					", uuid bigint not null" +
//					", create_date char(8)" +
//					", dbdate date" +
//					", dbtime time" +
//					", dbtimestamp TIMESTAMP" +
//					", money decimal(16, 2)" +
//					", status char(1) default '0'" +
//					")");
//		db.close();
//		assertThat(db.getName(), equalTo(DbinfoHelper.DEFAULT_DBNAME));
//	}
//	@AfterClass
//	public static void end() {
//		DatabaseWrapper db = null;
//		try {
//			db = new DatabaseWrapper.Builder().create();
//			db.ExecDDL("drop table " + testTableName);
//		} finally {
//			if(db!=null) db.close();
//		}
//	}

	@Test
	public void test() throws IOException {
		Files.walk(Paths.get("/tmp/fdcodegen/src/test/resources"),1).sorted(Comparator.reverseOrder())
				.map(Path::toFile)
//				.peek(System.out::println)
				.forEach(System.out::println);
		;
	}

	@Test
	public void t1_initProjectStruct() throws IOException {
		BaseCodeCreator creator = new BaseCodeCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir);
		creator.clearAndCreateProjectDirs();

		Path rootPath = Paths.get(creator.getSrcRootPath());
		assertThat(Files.isDirectory(rootPath), is(true));
		Files.walk(rootPath).forEach(path -> {
			assertThat(path.toString(), path.toFile().isDirectory(), is(true)); // 清理重建后，应该只有目录，没有其他
		});
	}

	@Test
	public void t2_makeupProject() {
		new ProjectStructCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir)
				.genGeneralJavaCodeFiles();
	}

	@Test
	public void t3_makeupResource() {
		new ProjectStructCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir)
				.genResourceFiles();
	}

	@Test
	public void t4_makeupEntity() {
		new ProjectStructCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir)
				.genEntityCodeFiles(DbinfosConf.DEFAULT_DBNAME);
	}

	@Test
	public void t5_makeupGradle() {
		new ProjectStructCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir)
				.genGradleFiles();
	}

	// 自己编写模版文件，自己写生成代码文件的程序
	@Test
	public void t9_makeupCustomFile() {
		Map<String, Object> ftlArgsValueMap = new HashMap<>();
		ftlArgsValueMap.put("tableName", "__first_used_entity_34678_");
		ftlArgsValueMap.put("className", "FirstUsedEntity"); // 注释掉，看看生成的文件是什么
		List<String> pks = new ArrayList<>(); pks.add("userid"); pks.add("roleid");
		ftlArgsValueMap.put("pkNameList", pks);

		List<ColumnMeta> columnMetaList = new ArrayList<>();
		columnMetaList.add(new ColumnMeta("te_operid", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("te_name", Types.VARCHAR));
		columnMetaList.add(new ColumnMeta("te_dept_type", Types.CHAR));
		columnMetaList.add(new ColumnMeta("te_role_id", Types.VARCHAR));
		ftlArgsValueMap.put("columnMetaList", columnMetaList);

		ftlArgsValueMap.put("destCodefileName", "FirstUsedEntity.java");

		new ProjectStructCreator("/tmp/fdcodegen", "hmfms.xxx"
				, ftlRootDir)
				.genCustomCodeFiles(
						BaseCodeCreator.TEMPLATE_TYPE_MAIN_JAVA,
						"entity",
						"FirstUsedEntity.java.ftl",
						ftlArgsValueMap);
	}

	@Test
	public void makeupAll() {
		// 因为依赖了core, database等模块，所以：
		// 1） 如果修改了程序，需在本工程根目录下执行： gradle copyJars
		// 2） 如果希望把上一步骤生成的jar包复制到指定目录，需修改本工程的根build.gradle的toRootDir变量值
		CmdMain codegenMain = new CodegenMain(new String[]{
				  "codedir=/java/app/intellij2018/example/A"
				, "basepkg=test.yyy"
				, "ftldir="+ ftlRootDir
				, "-E"
		});
		codegenMain.usage();
		codegenMain.start();
		TestCaseLog.println();
		TestCaseLog.println("删除 A 包build.gradle的内容");
		TestCaseLog.println("因为依赖了core, database，所以，如果feedwork被修改过，需重新执行 gradle copyJars");
	}

	@Test
	public void testDeleteDir() {

		try {
			FileUtil.cleanDirectory(new File("/tmp/xxx/deleteTest/src"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		CmdMain codegenMain = new CodegenMain(new String[]{
				"codedir=/tmp/xxx/deleteTest"
				, "basepkg=test.deletedir"
				, "ftldir="+ ftlRootDir
				, "-E"
		});
		codegenMain.start();

	}
}