package fd.ng.db.conf;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.db.jdbc.nature.*;

public enum Dbtype {
	ORACLE("oracle9iAbove") {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return Oracle9iAbove.toPagedSql(orginSql, orginBegin, orginEnd);
		}
	},
	POSTGRESQL("postgresql") {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return PostgreSQL.toPagedSql(orginSql, orginBegin, orginEnd);
		}
	},
	MYSQL("mysql") {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return MySQL.toPagedSql(orginSql, orginBegin, orginEnd);
		}
		@Override
		public String ofCountSql(final String sql) {
			return AbstractNatureDatabase.getCountSqlType2(sql);
		}
	},
	MARIADB("mariadb"),
	DB2V1("db2v1")  {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return DB2V1.ofPagedSql(orginSql, orginBegin, orginEnd);
		}
	},
	DB2V2("db2v2") {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return DB2V2.ofPagedSql(orginSql, orginBegin, orginEnd);
		}
	},
	SQLSERVER("sqlserver"), SYBASE("sybase"), ACCESS("access"),
	HIVE("hive") {
		@Override
		public PagedSqlInfo ofPagedSql(final String orginSql, final int orginBegin, final int orginEnd) {
			return DB2V2.ofPagedSql(orginSql, orginBegin, orginEnd);
		}
	},
	HANA("hana"),
	GBASE("gbase"),
	TERADATA("teradata"),
	GREENPLUM("gpdb"),
	NONE("none"), ANYONE("anyone");

	Dbtype(String desc) { this.desc = desc; }
	private final String desc;
	public String getDesc() { return desc; }

	/**
	 * 转换SQL成可进行分页查询的SQL，并且计算对应的分页参数。
	 * @param orginSql   原始SQL
	 * @param orginBegin 查询的开始下标，必须大于或等于1
	 * @param orginEnd   查询的结束下标
	 * @return PagedSqlInfo 包括转化成分页的SQL，已经对应的分页值
	 */
	public PagedSqlInfo ofPagedSql(String orginSql, final int orginBegin, final int orginEnd) {
		throw new FrameworkRuntimeException("You must impl this method for " + desc);
	}
	/**
	 * 根据原始 sql ，生成用于计算查询数据量的 count sql。
	 *
	 * @param sql 原始sql
	 * @return 用于计算原始sql查询数据量的 count sql
	 */
	public String ofCountSql(final String sql) {
		return AbstractNatureDatabase.getCountSqlType1(sql);
	}
}
