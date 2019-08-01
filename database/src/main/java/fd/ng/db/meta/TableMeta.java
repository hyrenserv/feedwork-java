package fd.ng.db.meta;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TableMeta {
	private static final Logger logger = LogManager.getLogger(TableMeta.class.getName());
	private String tableName; // 原始表名
	private String tableNameLower; // 转成小写的表名
	private String type; // 类型 TABLE 或 VIEW
	private String dbname; // 所属库名
	private String username; // 所属用户名
	private String remarks; // 表备注
	private Map<String, ColumnMeta> columnMetas; // 表的列信息
	private Set<String> primaryKeys; // 表主键

	public TableMeta() {
		columnMetas = new HashMap<>(0);
		primaryKeys = new HashSet<>(0);
	}
	public TableMeta(int columnNums) {
		columnMetas = new HashMap<>(columnNums);
		primaryKeys = new HashSet<>(1);
	}
	public String getTableName() {
		return tableName;
	}
	public String getTableNameLower() {
		return tableNameLower;
	}

	public String getType() {
		return type;
	}

	public String getDbname() {
		return dbname;
	}

	public String getUsername() {
		return username;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
		this.tableNameLower = tableName.toLowerCase();
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Set<String> getPrimaryKeys() {
		return primaryKeys;
	}

	public void addPrimaryKey(String primaryKey) {
		this.primaryKeys.add(primaryKey);
	}

	public Map<String, ColumnMeta> getColumnMetas() {
		return columnMetas;
	}
	public Set<String> getColumnNames() {
		return columnMetas.keySet();
	}

	public void addColumnMeta(ColumnMeta columnMeta) {
		if(columnMetas.containsKey(columnMeta.getName())) logger.warn("tableName={} already exists", columnMeta.getName());
		columnMetas.put(columnMeta.getName(), columnMeta);
	}

	public ColumnMeta getColumnMeta(String colunmName) {
		return columnMetas.get(colunmName);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TableMeta.class.getSimpleName() + "[", "]")
				.add("tableName='" + tableName + "'")
				.add("type='" + type + "'")
				.add("dbname='" + dbname + "'")
				.add("username='" + username + "'")
				.add("remarks='" + remarks + "'")
				.add("columnMetas=" + columnMetas)
				.toString();
	}
}
