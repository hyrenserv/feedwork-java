package fd.ng.web.hmfmswebapp.entity;

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

@Table(tableName = "__user_fd_unittest_17456")
public class User extends TableEntity{
	public static String pk = "name";
	private transient static final Set<String> __PrimaryKeys;
	private String name;
	private int age;
	private String password;
	@Column("class")
	private String uclass;
	private String create_time;

	static {
		Set<String> tmpPrimaryKeys = new HashSet<>();
		tmpPrimaryKeys.add("name");
		__PrimaryKeys = Collections.unmodifiableSet(tmpPrimaryKeys);
	}
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	public User() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("age=" + age)
				.add("password='" + password + "'")
				.add("create_time='" + create_time + "'")
				.toString();
	}
}
