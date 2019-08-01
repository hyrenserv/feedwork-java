package fd.ng.db.entity;

import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Table(tableName = "__TableEntityTest_fd_17456" )
public class TableEntityTestUser extends TableEntity {
	private String name;
	private int age;
	@Column("class")
	private String uclass;
	private String create_date;
	private transient static final Set<String> __PrimaryKeys;
	static {
		Set<String> tmpPrimaryKeys = new HashSet<>();
		tmpPrimaryKeys.add("name");
		__PrimaryKeys = Collections.unmodifiableSet(tmpPrimaryKeys);
	}
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	public TableEntityTestUser() {

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

	public String getUclass() {
		return uclass;
	}

	public void setUclass(String uclass) {
		if(uclass==null) addNullValueField("uclass");
		this.uclass = uclass;
	}

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		if(create_date==null) addNullValueField("create_date");
		this.create_date = create_date;
	}
}
