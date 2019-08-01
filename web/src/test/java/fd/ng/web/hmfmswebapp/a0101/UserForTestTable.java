package fd.ng.web.hmfmswebapp.a0101;

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// 该表在 TestStageWebServerLauncher 启动时被创建
@Table(tableName = UserForTestTable.TableName)
public class UserForTestTable extends TableEntity {
	public static final String TableName = "__user_fd_unittest_17456";

	private String name;
	private String password;
	private Integer age;
	private String create_time;
	private transient static final Set<String> __PrimaryKeys;
	static {
		Set<String> tmppks = new HashSet<>();
		tmppks.add("name");
		__PrimaryKeys = Collections.unmodifiableSet(tmppks);
	}
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name==null) addNullValueField("name");
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if(password==null) addNullValueField("password");
		this.password = password;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		if(age==null) addNullValueField("age");
		this.age = age;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		if(create_time==null) addNullValueField("create_time");
		this.create_time = create_time;
	}
}
