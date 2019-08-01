package fd.ng.db.meta;

import fd.ng.db.util.TypeMapper;

import java.util.StringJoiner;

public class ColumnMeta {
	private String name;
	private String alias;
	private int typeOfSQL;     // rsmd.getColumnType 得到的类型数字，也就是 java.sql.Types 里面的类型数字，比如Types.VARCHAR
	private String typeOfJava;
	private String typeName;
	private boolean nullable;
	private int length;
	private int scale;
	private String remark;

	public ColumnMeta() {}

	public ColumnMeta(String name, int typeOfSQL) {
		this.name = name;
		this.typeOfSQL = typeOfSQL;
		this.typeOfJava = TypeMapper.getJavaType(this.typeOfSQL).getSimpleName();
	}

	public String getName() {
		return name;
	}

	public String getAlias() {
		return alias;
	}

	public int getTypeOfSQL() {
		return typeOfSQL;
	}
	public String getTypeOfJava() {
		return typeOfJava;
	}

	public String getTypeName() {
		return typeName;
	}

	public boolean isNullable() {
		return nullable;
	}

	public int getLength() {
		return length;
	}

	public int getScale() {
		return scale;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setTypeOfSQL(int typeOfSQL) {
		this.typeOfSQL = typeOfSQL;
		this.typeOfJava = TypeMapper.getJavaType(this.typeOfSQL).getSimpleName();
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ColumnMeta.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("alias='" + alias + "'")
				.add("typeOfSQL=" + typeOfSQL)
				.add("typeOfJava='" + typeOfJava + "'")
				.add("typeName='" + typeName + "'")
				.add("nullable=" + nullable)
				.add("length=" + length)
				.add("scale=" + scale)
				.add("remark='" + remark + "'")
				.toString();
	}
}
