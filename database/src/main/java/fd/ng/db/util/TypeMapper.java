package fd.ng.db.util;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeMapper {
	private TypeMapper() {}
	// key : DB中的类型  value ：对应的java数据类型
	private static final Map<Integer, Class<?>> _typeMapper;
	static {
		Map<Integer, Class<?>> _typeMapperBox = new HashMap<>();
		_typeMapperBox.put(Types.VARCHAR, String.class);
		_typeMapperBox.put(Types.LONGNVARCHAR, String.class);
		_typeMapperBox.put(Types.NCHAR, String.class);
		_typeMapperBox.put(Types.NVARCHAR, String.class);
		_typeMapperBox.put(Types.LONGVARCHAR, String.class);
		_typeMapperBox.put(Types.CHAR, String.class); // maybe char[] ?

		_typeMapperBox.put(Types.BIGINT, Long.class);

		_typeMapperBox.put(Types.INTEGER, Integer.class);
		_typeMapperBox.put(Types.SMALLINT, Integer.class); // maybe byte ?
		_typeMapperBox.put(Types.TINYINT, Integer.class);

		_typeMapperBox.put(Types.BOOLEAN, Boolean.class);

		_typeMapperBox.put(Types.DECIMAL, BigDecimal.class);
		_typeMapperBox.put(Types.NUMERIC, BigDecimal.class);
		_typeMapperBox.put(Types.FLOAT, BigDecimal.class);
		_typeMapperBox.put(Types.DOUBLE, BigDecimal.class);
		_typeMapperBox.put(Types.REAL, BigDecimal.class);

		_typeMapperBox.put(Types.DATE, LocalDate.class); // maybe String
		_typeMapperBox.put(Types.TIME_WITH_TIMEZONE, ZonedDateTime.class);
		_typeMapperBox.put(Types.TIME, LocalTime.class);
		_typeMapperBox.put(Types.TIMESTAMP, Instant.class);

		_typeMapperBox.put(Types.JAVA_OBJECT, Object.class); // correct ?
		_typeMapperBox.put(Types.OTHER, Object.class); // correct ?
		_typeMapperBox.put(Types.NULL, null); // correct ?
		_typeMapperBox.put(Types.ARRAY, Object[].class); // maybe String[] ?

		_typeMapperBox.put(Types.LONGVARBINARY, byte[].class);

		_typeMapper = Collections.unmodifiableMap(_typeMapperBox);
	}

	public static Class<?> getJavaType(int sqlType) {
		Class<?> c = _typeMapper.get(sqlType);
		if(c==null) throw new FrameworkRuntimeException("No mapping for sql_type="+sqlType);
		return c;
	}
}
