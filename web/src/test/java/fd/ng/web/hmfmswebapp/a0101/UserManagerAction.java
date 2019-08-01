package fd.ng.web.hmfmswebapp.a0101;

import fd.ng.core.utils.DateUtil;
import fd.ng.db.conf.DbinfosConf;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.web.util.Dbo;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.exception.AppSystemException;
import fd.ng.web.exception.BusinessException;
import fd.ng.web.util.RequestUtil;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 这是一个样例程序，完成了对一张表的增删改查操作。
 * 每个方法可以有任意类型的入参，系统会自动为每个参数装配值。
 * 返回值类型任意，系统把转成 json 格式返回前端。
 * Action 类是无状态的，也就是说，不允许有成员变量（除非是 static final 型）
 */
public class UserManagerAction extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(UserManagerAction.class.getName());

	public String index() {
		return "index";
	}

	public String session() {
		int maxInactiveInterval = RequestUtil.getSession().getMaxInactiveInterval();
		logger.info("maxInactiveInterval={}", maxInactiveInterval);
		return "session";
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
				"insert into "+ UserForTestTable.TableName +
						"(name, age, create_time) values(?, ?, ?)",
				name, age, DateUtil.getDateTime()
		);
		if( nums==1 ) return true;
		else throw new BusinessException("失败");
	}
	public boolean addUserByEntity(UserForTestTable user) {
		int nums = user.add(Dbo.db());
		if( nums==1 ) return true;
		else return false;
	}

	public void updateUser(String name, int age) {
		UserForTestTable user = new UserForTestTable();
		user.setName(name);
		user.setAge(age);
		if(user.update(Dbo.db())!=1)
			throw new BusinessException( String.format("更新数据失败！name=%s, age=%s", name, age) );
	}

	public boolean delUser(String name) {
		int nums = Dbo.execute("delete from "+ UserForTestTable.TableName+" where name = ?", name);
		if( nums==1 ) return true;
		else return false;
	}

	public List<Map<String, Object>> getAllUsersForMapList() {
		List<Map<String, Object>> result = Dbo.queryList(
				"select * from "+ UserForTestTable.TableName+" order by create_time desc limit 10");
		return result;
	}

//	public UserForTestTable getUser(String name) {
//		Optional<UserForTestTable> result = Dbo.queryOneObject(UserForTestTable.class,
//				"select * from "+ UserForTestTable.TableName+" where name=?", name);
//		return result.get();
//	}
	public Optional<UserForTestTable> getUser(String name) {
		return Dbo.queryOneObject(UserForTestTable.class,
				"select * from "+ UserForTestTable.TableName + " where name=?", name);
	}

	public Map<String, Object> getPagedUserResult(int currPage, int pageSize) {
		Page page = new DefaultPageImpl(currPage, pageSize);
		Result result0 = Dbo.queryPagedResult(page,
				"select * from "+ UserForTestTable.TableName+" order by create_time desc");
		Map<String, Object> result = new HashMap<>();
		result.put("count", page.getTotalSize());
		result.put("data", result0.toList());
		return result;
	}

	public Map<String, Object> getPagedUserResultNoCount(int currPage, int pageSize) {
		Page page = new DefaultPageImpl(currPage, pageSize, false);
		Result result0 = Dbo.queryPagedResult(page,
				"select * from "+ UserForTestTable.TableName+" order by create_time desc");
		Map<String, Object> result = new HashMap<>();
		result.put("count", page.getTotalSize());
		result.put("data", result0.toList());
		return result;
	}

	public List<UserForTestTable> getPagedUsers(int currPage, int pageSize) {
		UserForTestTable user = new UserForTestTable();
		Page page = new DefaultPageImpl(currPage, pageSize);
		List<UserForTestTable> result = Dbo.queryPagedList(UserForTestTable.class, page,
				"select * from "+ UserForTestTable.TableName+" order by create_time desc");
		return result;
	}
}
