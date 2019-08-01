package ${basePackage}.${subPackage};

import fd.ng.core.utils.DateUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.annotation.UploadFile;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.FileUploadUtil;
import fd.ng.web.util.RequestUtil;

import ${basePackage}.exception.AppSystemException;
import ${basePackage}.exception.BusinessException;
import ${basePackage}.biz.zbase.WebappBaseAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 这是一个样例程序，完成了对一张表的增删改查操作。
 * 该表需要提前创建到数据库
 * 用浏览器访问本类的 initTestTable7456 方法即可完成建表操作:
 * http://ip:port/.../action/${basePackage?replace(".", "/")}/biz/ztest/initTestTable7456
 */
public class FuncStressTestAction extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger();

	public String initTestTable7456() {
		if (Dbo.db().isExistTable(FuncStressTestEntity.TableName))
			return "测试用表：["+FuncStressTestEntity.TableName+"] 已经存在！";
		else
			Dbo.db().ExecDDL("create table " + FuncStressTestEntity.TableName +
					"( name varchar(48) not null primary key, " +
					"password varchar(20), age int, create_time varchar(20))");
		// 初始插入1条数据
		int nums = Dbo.execute("insert into " + FuncStressTestEntity.TableName +
				" values('aoot', '11111', 80, '" + DateUtil.getDateTime() + "' )");
		if (nums != 1) {
			Dbo.rollbackTransaction();
			throw new BusinessException("init data for table[ " + FuncStressTestEntity.TableName + "] failed!");
		}
		Dbo.commitTransaction();
		return "创建测试表成功";
	}

	// 测试海量循环中频繁获取 DB 对象的性能
	public long perfAmountGetJdbcWraper() {
		DatabaseWrapper db0 = Dbo.db();
		DatabaseWrapper db1 = Dbo.db();
		long start = System.currentTimeMillis();
		//100万次在笔记本上50毫秒以下
		for(int i=0; i<1000000; i++) {
			DatabaseWrapper dbn = Dbo.db();
		}
		long et = System.currentTimeMillis()-start;
		return et;
	}

	public boolean testUsedDB() {
		DatabaseWrapper db0 = Dbo.db(); // 取得当前在用的 db
		if(db0==null) return false;
		if(!db0.getName().equals(DbinfosConf.DEFAULT_DBNAME)) return false;

		DatabaseWrapper db0_1 = Dbo.db(DbinfosConf.DEFAULT_DBNAME);
		if(db0_1==null) return false;
		if(!db0_1.toString().equals(db0.toString())) return false;

		DatabaseWrapper db1 = Dbo.db("xxxlkjsdf 90843"); // 不存在的DB
		if(db1!=null) return false;

		return true;
	}

	public Map<String, Object> welcome(String name) {
		Person person = new Person(name, 99, "男", new String[]{"xxx", "yyy"}, new BigDecimal("234.99"));
		Map<String, Object> map = new HashMap<>();
		map.put("person", person);
		map.put("action", "welcome");
		map.put("time", DateUtil.getDateTime());
		map.put("LocalDate", LocalDate.now());

		Map<String, String> loginUser = RequestUtil.getSessValue("user");
		map.put("loginuser", loginUser==null?"null":loginUser.get("username"));

		// 这是在 WrapFdwebFilter 里面设置的值
		String newValue = (String)RequestUtil.getRequest().getAttribute("__newValue");
		map.put("newValue", newValue==null?"null-newValue":newValue+" in welcome");

		return map;
	}

	public Map<String, Object> bean(@RequestBean Person person) {
		Map<String, Object> map = new HashMap<>();
		map.put("person", person);
		map.put("action", "welcome");
		map.put("time", DateUtil.getDateTime());
		map.put("LocalDate", LocalDate.now());

		Map<String, String> loginUser = RequestUtil.getSessValue("user");
		map.put("loginuser", loginUser==null?"null":loginUser.get("username"));

		// 这是在 WrapFdwebFilter 里面设置的值
		String newValue = (String)RequestUtil.getRequest().getAttribute("__newValue");
		map.put("newValue", newValue==null?"null-newValue":newValue+" in welcome");

		return map;
	}

	// ---------------  测试业务异常  ---------------

	public Map<String, String> bizExcetion_OnlyMsg() {
		throw new BusinessException("only msg");
	}
	public Map<String, String> bizExcetion_ResNoArgs() {
		throw new BusinessException("hmfms.a0101.userlist", null);
	}
	public Map<String, String> bizExcetion_ResHasArgs() {
		throw new BusinessException("hmfms.a0101.userinfo", new Object[]{
				"FD飞", 123
		});
	}
	public Map<String, String> bizSysExcetion_OnlyEx() {
		throw new AppSystemException(new SQLException("only SQLException"));
	}
	public Map<String, String> bizSysExcetion_MsgAndEx() {
		throw new AppSystemException("MsgAndEx", new SQLException("SQLException Info"));
	}
	public Map<String, String> bizSysExcetion_MsgAndLogAndEx() {
		throw new AppSystemException("MsgAndLogAndEx", "logged MSG", new SQLException("SQLException Info"));
	}

	// ---------------  测试有数据操作的功能  ---------------

	public boolean addUser(String name, int age) {
		int nums = Dbo.execute(
				"insert into "+ FuncStressTestEntity.TableName +
						"(name, age, create_time) values(?, ?, ?)",
				name, age, DateUtil.getDateTime()
		);
		if( nums==1 ) return true;
		else throw new BusinessException("失败");
	}
	public boolean addUserByEntity(FuncStressTestEntity user) {
		int nums = user.add(Dbo.db());
		if( nums==1 ) return true;
		else return false;
	}

	public boolean updateUser(FuncStressTestEntity user) {
		int nums = user.update(Dbo.db());
		if( nums==1 ) return true;
		else return false;
	}

	public boolean delUser(String name) {
		int nums = Dbo.execute("delete from "+ FuncStressTestEntity.TableName+" where name = ?", name);
		if( nums==1 ) return true;
		else return false;
	}

	public List<Map<String, Object>> getAllUsersForMapList() {
		List<Map<String, Object>> result = Dbo.queryList(
				"select * from "+ FuncStressTestEntity.TableName+" order by create_time desc limit 10");
		return result;
	}

	public Optional<FuncStressTestEntity> getUser(String name) {
		return Dbo.queryOneObject(FuncStressTestEntity.class,
				"select * from "+ FuncStressTestEntity.TableName+" where name=?", name);
	}

	public Map<String, Object> getPagedUserResult(int currPage, int pageSize) {
		Page page = new DefaultPageImpl(currPage, pageSize);
		Result result0 = Dbo.queryPagedResult(page,
				"select * from "+ FuncStressTestEntity.TableName+" order by create_time desc");
		Map<String, Object> result = new HashMap<>();
		result.put("count", page.getTotalSize());
		result.put("data", result0.toList());
		return result;
	}

	public Map<String, Object> getPagedUserResultNoCount(int currPage, int pageSize) {
		Page page = new DefaultPageImpl(currPage, pageSize, false);
		Result result0 = Dbo.queryPagedResult(page,
				"select * from "+ FuncStressTestEntity.TableName+" order by create_time desc");
		Map<String, Object> result = new HashMap<>();
		result.put("count", page.getTotalSize());
		result.put("list", result0.toList());
		return result;
	}

	public List<FuncStressTestEntity> getPagedUsers(int currPage, int pageSize) {
		FuncStressTestEntity user = new FuncStressTestEntity();
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<FuncStressTestEntity> result = Dbo.queryPagedList(FuncStressTestEntity.class, page,
				"select * from "+ FuncStressTestEntity.TableName+" order by create_time desc");
		return result;
	}

	@UploadFile
	public String uploadfiles(String someValue1, int someValue2, String savedDir, String[] uploadFiles) throws IOException {
		StringBuilder ret = new StringBuilder(someValue1 + " | " + someValue2);
		// 循环处理每个上传的文件
		for(String upFileinfo : uploadFiles) {
			File uploadedFile = FileUploadUtil.getUploadedFile(upFileinfo);
			if(!uploadedFile.isFile()) {
				String msg = "upload file[" + uploadedFile.getName() + "] failed";
				throw new BusinessException(msg);
			}
			else {
				// 文件上传成功，编写自己的业务处理逻辑。
				// 比如，把上传的文件移动到某个目录下，并且命名成原始上传的文件名：
				String orgnFilename = FileUploadUtil.getOriginalFileName(upFileinfo);
				Path newFilePath = Paths.get(savedDir + orgnFilename);
				Files.move(uploadedFile.toPath(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

				ret.append(" | ").append(orgnFilename);
			}
		}

		return ret.toString();
	}
}
