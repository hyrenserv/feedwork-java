package ${basePackage}.${subPackage};

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import ${basePackage}.exception.BusinessException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

@Table(tableName = "uat_fdt_User_${.now?string["yyMMdd"]}")
public class ${className} extends TableEntity {
	private transient static final Set<String> __PrimaryKeys;
	private String name;
	private int age;
	private String password;
	private String create_time;
	@Column("class")
	private String uclass; // 测试对字段注解用

	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("name");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}

	public ${className}() {

	}

	public String getUclass() {
		return uclass;
	}

	public void setUclass(String uclass) {
		if(uclass==null) addNullValueField("uclass");
		this.uclass = uclass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name==null) throw new BusinessException("Entity : ${className}.name must not null!");
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getPassword() {
		if(password==null) addNullValueField("password");
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		if(create_time==null) addNullValueField("create_time");
		this.create_time = create_time;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ${className}.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("age=" + age)
				.add("uclass='" + uclass + "'")
				.add("password='" + password + "'")
				.add("create_time='" + create_time + "'")
				.toString();
	}
}
