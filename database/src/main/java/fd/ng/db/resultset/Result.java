package fd.ng.db.resultset;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.Validator;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public class Result {
	public static final BigDecimal BIGDECIMAL_ZERO = new BigDecimal("0.00");
//	static {
//		// 这里一定要注册默认值，使用null也可以
//		BigDecimalConverter bd = new BigDecimalConverter(null);//或者使用上面的BIGDECIMAL_ZERO
//		ConvertUtils.register(bd, java.math.BigDecimal.class);
//	}
	private List<Map<String, Object>> results;
//	private Page page = null;
	private int toStringFormatWidth = 20;

	/**
	 * 构造一个空的结果集
	 */
	public Result(){
		this.results = new ArrayList<>();
	}

	/**
	 * 构造一个结果集。List中的每一行为一个Map。<br>
	 * 构造样例：<br>
	 * List list = new ArrayList();
	 * Map row1 = new HashMap();
	 * row1.put("name", "fd");
	 * row1.put("money", new BigDecimal("23.34"));
	 * list.add(row1);
	 * Result rs = new Result(list);
	 * @param results
	 */
	public Result(List<Map<String, Object>> results) {
		Validator.notNull(results);
		this.results = results;
	}

	/**
	 * 把Result转换成List。<br>
	 * List中的每一行是一个Map。
	 * @return
	 */
	public List<Map<String, Object>> toList()
	{
		return(results);
	}

	/**
	 * 把结果集转换成JSON。<br>
	 * 注意：对于BigDecimal类型的数据，会出现精度问题。用String创建的BigDecimal不会有精度问题。
	 * @return
	 */
	public String toJSON()
	{
		return JsonUtil.toJson(results);
	}

	public void setToStringFormatWidth(int toStringFormatWidth)
	{
		this.toStringFormatWidth = toStringFormatWidth;
	}

//	/**
//	 * 返回一行数据。
//	 * @param irow int 从0开始的行号。
//	 * @return
//	 */
//	public ResultRow getResultRow(int irow)
//	{
//		if( irow>=results.size()||irow<0 )
//			return null;
//		Map<String, Object> map = results.get(irow);
//		return new ResultRow(map);
//	}
//	/**
//	 * 增加一行数据。
//	 * @param row ResultRow
//	 * @return
//	 */
//	public void add(ResultRow row)
//	{
//		results.add(row.toMap());
//	}
	/**
	 * 返回结果集里面的所有列名字。
	 * @return List<String>
	 */
	public List<String> getColumnsName()
	{
		if(results.size()<1)
			return Collections.emptyList();

		return new ArrayList<>(results.get(0).keySet());
	}
	/**
	 * 在原来的结果集合后面增加多行。<br>
	 * 新增加的对象中的MAP的KEY名字一定要跟原来的MAP一致，否则取出来都是NULL
	 *
	 * @param o
	 *            行对象
	 */
	public void add(List<Map<String, Object>> o)
	{
		results.addAll(o);
	}

	/**
	 * 在原来的结果集合的指定位置增加多行。<br>
	 * 新增加的对象中的MAP的KEY名字一定要跟原来的MAP一致，否则取出来都是NULL
	 *
	 * @param index
	 *            插入的位置
	 * @param o
	 *            行对象
	 */
	public void add(int index, List<Map<String, Object>> o)
	{
		if( index < 0 )
			return;
		if( index > results.size() )
			index = results.size();
		results.addAll(index, o);
	}

	/**
	 * 在原来的结果集合后面增加多行。<br>
	 * 新增加的对象中的MAP的KEY名字一定要跟原来的MAP一致，否则取出来都是NULL
	 *
	 * @param o
	 *            行对象
	 */
	public void add(Result o)
	{
		results.addAll(o.toList());
	}

	/**
	 * 在原来的结果集合的指定位置增加多行。<br>
	 * 新增加的对象中的MAP的KEY名字一定要跟原来的MAP一致，否则取出来都是NULL
	 *
	 * @param index
	 *            插入的位置
	 * @param o
	 *            行对象
	 */
	public void add(int index, Result o)
	{
		if( index < 0 )
			return;
		if( index > results.size() )
			index = results.size();
		results.addAll(index, o.toList());
	}

	/**
	 * 把两个结果集合横向合并
	 *
	 * @param newList
	 *            被合并的结果集合
	 * @throws FrameworkRuntimeException
	 *             如果两个结果集合大小不相等
	 */
	public void populate(List<Map<String, Object>> newList)
	{
		//List newList = newResult.toList();
		if( (results == null || results.size() < 1) && (newList == null || newList.size() < 1) )
			return;
		if( results == null || results.size() < 1 )
		{
//			results = new ArrayList<>();
			results.addAll(newList);
		}
		if( newList == null || newList.size() < 1 )	{
			return;
		}
		int size = results.size();
		if( size != newList.size() )
			throw new FrameworkRuntimeException("结果集合行数不相同，不能合并！");

		List<Map<String, Object>> oldList = new ArrayList<>(results.size());
		oldList.addAll(results);
		for(int i=0; i<size; i++)
		{
			Map<String, Object> row = oldList.get(i);
			Map<String, Object> newRow = newList.get(i);

			row.putAll(newList.get(i));
		}
		//以下两句或许应该被同步！！！！！！！！！！！！！！！！！！！！
		results.clear();
//		results.addAll(oldList);
		results = oldList;
	}

	/**
	 * 把两个结果集合横向合并
	 *
	 * @param newResult
	 *            被合并的结果集合
	 * @throws FrameworkRuntimeException
	 *             如果两个结果集合大小不相等
	 */
	public void populate(Result newResult)
	{
		populate(newResult.toList());
	}

	/**
	 * 把一列字符数据横向拼装进Result
	 *
	 * @param columnName
	 *            新合并的列的名字【注意：名字必须是小写！！！】
	 * @param columnValues
	 *            新列的数值
	 * @throws FrameworkRuntimeException
	 *             如果两个结果集合大小不相等，即：columnValues.length!=Result.getRowCount()
	 */
	public void populate(String columnName, String[] columnValues)
	{
		int size = results.size();
		if( size != columnValues.length )
			throw new FrameworkRuntimeException("结果集合行数不相同，不能合并！");

		List<Map<String, Object>> oldList = new ArrayList<>(results.size());
		oldList.addAll(results);

		for(int i = 0; i < oldList.size(); i++)
		{
			Map<String, Object> row = oldList.get(i);
			row.put(columnName.toLowerCase(), columnValues[i]);
		}

		//以下两句或许应该被同步！！！！！！！！！！！！！！！！！！！！
		results.clear();
//		results.addAll(oldList);
		results = oldList;
	}

//	/**
//	 * 生成以某些列为KEY的MAP，VALUE为一行数据。KEY值必须是STRING
//	 *
//	 * @param keyArray
//	 * @return
//	 */
//	public FastHashMap genMapedResult(String[] keyArray)
//	{
//		FastHashMap returnMap = new FastHashMap();
//		if( keyArray.length < 1 )
//			throw new IllegalArgumentException("key can not null!");
//		if( results == null )
//			throw new IllegalArgumentException("result can not null!");
//		int size = results.size();
//		for(int i=0; i<size; i++)
//		{
//			List row = new ArrayList();
//			Map curRow = (Map)results.get(i);
//			row.add(curRow);
//			Result rsRow = new Result(row);
//			String keys = "";
//			for(int j=0; j<keyArray.length; j++)
//			{
//				keys += "[" + (curRow.get(keyArray[j])).toString() + "]";
//			}
//			if( true )//如果该key对应的数据已经在map中存在，则抛出异常
//			{
//				Result rsTmpRow = (Result)returnMap.get(keys);
//				if( rsTmpRow != null ) {
//					throw new FrameworkRuntimeException("重复的KEY值 : " + keys);
//				}
//			}
//			returnMap.put(keys, rsRow);
//		}
//		return returnMap;
//	}
//
//	public static Result getResultByKeys(String[] keyArray, FastHashMap mapedResult)
//	{
//		if( keyArray.length < 1 )
//			throw new IllegalArgumentException("key can not null!");
//		String keys = "";
//		for(int j=0; j<keyArray.length; j++)
//		{
//			keys += "[" + keyArray[j] + "]";
//		}
//		return (Result)mapedResult.get(keys);
//	}

	/**
	 * 判断结果集中是否有数据。等同于getRowCount()<1。
	 * @return
	 */
	public boolean isEmpty()
	{
		return results.isEmpty();
	}

	/**
	 * 获得结果集的记录行数。
	 * @return
	 */
	public int getRowCount() {
		return results.size();
	}

	public Object getObject(int irow, String columnName)
	{
		if( irow >= results.size() )
			return null;
		Map<String, Object> row = results.get(irow);
		Object obj = row.get(columnName);
		return obj;
	}

	/**
	 * 给结果集添加数据
	 * @param irow 给第几行添加数据
	 * @param columnName
	 * @param obj
	 */
	public void setObject(int irow, String columnName, Object obj)
	{
//		if( obj==null )
//			throw new RuntimeException("param need to by setted should not be null!");
		if( irow >= results.size() ){
			return;//throw new RuntimeException("row=" + irow + " is Out Of Result max size!");
		}
		Map<String, Object> row = results.get(irow);
		row.put(columnName, obj);
	}

	public byte[] getBlob(int irow, String columnName)
	{
		Object obj = getObject(irow, columnName);
		if( obj==null )
			return null;
		if( obj instanceof byte[] ){//使用JDBC方式连接数据库的时候，就是这种类型
			return (byte[])obj;
		} else {
			throw new FrameworkRuntimeException("Not support : " + columnName);
		}
	}

	public String getClob(int irow, String columnName)
	{
		Object obj = getObject(irow, columnName);
		if( obj==null )
			return "";
		if( obj instanceof String ){//使用JDBC方式连接数据库的时候，就是这种类型
			return (String)obj;
		} else {
			throw new FrameworkRuntimeException("Not support : " + columnName);
		}
	}

	public String toString()
	{
		int size = getRowCount();
		if( size < 1 )
			return "Empty Result.";
		StringBuilder out = new StringBuilder("\n");
		Map<String, Object> tmpRow = results.get(0);
		Set<String> columnNames = tmpRow.keySet();
		columnNames.forEach(colName->out.append(formatWithSpaces(colName)));

		out.deleteCharAt(out.length()-2);
		int len = out.length();
		out.append("\n");
		for (int i=0; i < len-1; i++)
			out.append("-");
		out.append("\n");

		for (int i=0; i < getRowCount(); i++) {
			final int irow = i;
			columnNames.forEach(columnName->{
				String formattedColName = null;
				if (isNull(irow, columnName))
					formattedColName = formatWithSpaces("NULL");
				else
					formattedColName = formatWithSpaces(getString(irow, columnName));
				out.append(formattedColName);
			});
			out.deleteCharAt(out.length()-2);
			out.append("\n");
		}

		return(out.toString());
	}

	private String formatWithSpaces(String s)
	{
		StringBuilder sb = new StringBuilder(s);
		int width = toStringFormatWidth;
		int len = s.length();
		if (len < width)
		{
			//sb.insert(0,'|');
			for (int i=0; i < width-len; i++)
				sb.append(" ");
			return(sb.toString());
		}
		else
		{
			return(sb.substring(0, width));
		}
	}

	public boolean isNull(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		return(o == null);
	}

	/**
	 * Gets the value of the field corresponding to (row, col) index as an int
	 *
	 * @param row
	 * @param columnName
	 * @return int value of field
	 */
	public int getInt(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			throw new FrameworkRuntimeException("row=" + row + ", column=" + columnName + " is null!");
		if (o instanceof Integer)
			return (int)o;
		else if (o instanceof Long)
			return(((Long)o).intValue());
		else if( o instanceof String )
			return Integer.parseInt((String)o);
		else
			return(((BigDecimal)o).intValue());
	}

	/**
	 * 如果对应的数据库字段为空，则返回0.
	 * @param row
	 * @param columnName
	 * @return
	 */
	public int getIntDefaultZero(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return 0;
		else
			return getInt(row, columnName);
	}
	public Integer getInteger(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		else
			return getInt(row, columnName);
	}

	/**
	 * Gets the value of the field corresponding to row, col as a long
	 *
	 * @param row
	 * @param columnName
	 * @return long
	 */
	public long getLong(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			throw new FrameworkRuntimeException("row=" + row + ", column=" + columnName + " is null!");
		if (o instanceof Long)
			return (long)o;
		else if (o instanceof Integer)
			return(((Integer)o).longValue());
		else if( o instanceof String )
			return Long.parseLong((String)o);
		else
			return(((BigDecimal)o).longValue());
	}

	public long getLongDefaultZero(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return 0L;
		else
			return getLong(row, columnName);
	}

	public Long getLongObject(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		else
			return getLong(row, columnName);
	}

	/**
	 * Gets the value of the field corresponding to (row, col) index as a String
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return String value of field
	 */
	public String getString(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o == null )
			return "";
		if( o instanceof String )
			return (String)o;
		else if( o instanceof BigDecimal )
		{
			BigDecimal b = (BigDecimal)o;
			return(b.toString());
		}
		else if( o instanceof Long )
			return (((Long)o).toString());
		else if (o instanceof Integer)
			return(((Integer)o).toString());
		else if ((o instanceof java.sql.Date) || (o instanceof java.sql.Timestamp))
			return("" + getDate(row, columnName));
		else
		{
			String s = o.toString();
			return(s);
		}
	}

	/**
	 * Gets the value of the field corresponding to (row, col) index as a String
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @param newValue
	 *            新值
	 * @return String value of field
	 */
	public void setValue(int row, String columnName, String newValue)
	{
		setObject(row, columnName, newValue);
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a boolean
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return boolean value of field
	 */
	public boolean getBoolean(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			throw new FrameworkRuntimeException("row=" + row + ", column=" + columnName + " is null!");
		return  (Boolean)o;
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Date
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return Date value of field
	 */
	public Date getDate(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		if (o instanceof Timestamp)
		{
			Timestamp t = (Timestamp)o;
			Date d = new Date(t.getTime());
			return(d);
		}
		else
		{
			Date d = (Date)o;
			return(d);
		}
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Time
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return Time value of field
	 */
	public Time getTime(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		Time t = (Time)o;
		return(t);
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Timestamp
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return Timestamp value of field
	 */
	public Timestamp getTimestamp(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		if (o instanceof Date)
		{
			Date d = (Date)o;
			Timestamp t = new Timestamp(d.getTime());
			return(t);
		}
		else
		{
			Timestamp t = (Timestamp)o;
			return(t);
		}
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a double
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            列名字
	 * @return BigDecimal Object of field
	 */
	public BigDecimal getBigDecimal(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return null;
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b);
		}
		else
		{
			return(new BigDecimal(o.toString()));
		}
	}

	public BigDecimal getBigDecimalNotNull(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			return new BigDecimal(0);
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b);
		}
		else
		{
			return(new BigDecimal(o.toString()));
		}
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a double
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return double value of field
	 */
	public double getDouble(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			throw new FrameworkRuntimeException("row=" + row + ", column=" + columnName + " is null!");
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b.doubleValue());
		}
		else
		{
			Double d = (Double)o;
			return(d.doubleValue());
		}
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a float
	 *
	 * @param row
	 *            int
	 * @param columnName
	 *            int
	 * @return float value of field
	 */
	public double getFloat(int row, String columnName)
	{
		Object o = getObject(row, columnName);
		if( o==null )
			throw new FrameworkRuntimeException("row=" + row + ", column=" + columnName + " is null!");
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b.floatValue());
		}
		else
		{
			Float f = (Float)o;
			return(f.floatValue());
		}
	}

	/**
	 * 给结果集排序（升序）
	 * @param sortColumn 被排序的字段
	 */
	public void sortResult(final String sortColumn)
	{
		sortResult(sortColumn, "");
	}

	/**
	 * 给结果集排序
	 * @param sortColumn 被排序的字段
	 * @param desc "desc"为降序，"asc"为升序
	 */
	public void sortResult(final String sortColumn, final String desc)
	{
		List<Map<String, Object>> list = this.results;
		java.util.Collections.sort(list, new java.util.Comparator<Map<String, Object>>() {
					public final int compare(Map<String, Object> a, Map<String, Object> b) {
						final Object rowA = a.get(sortColumn);
						final Object rowB = b.get(sortColumn);

						int xiao = -1;
						int da = 1;
						if(rowA==null&&rowB==null) return 0;
						if(rowA==null) return xiao;
						if(rowB==null) return da;
						int compareResult = compareTo(rowA, rowB);
						return compareResult;
					}
				}
		);
		if(desc!=null&&desc.equalsIgnoreCase("desc")){
			java.util.Collections.reverse(list);
		}
	}
	private int compareTo(Object valueObj1, Object valueObj2)
	{

		if (valueObj1 instanceof Integer)
		{
			if ( !(valueObj2 instanceof Integer) ) return -1;
			int value1 = ((Integer)valueObj1).intValue();
			int value2 = ((Integer)valueObj2).intValue();
			if(value1<value2) return -1;
			else if(value1==value2) return 0;
			else return 1;
		}
		else if (valueObj1 instanceof Short)
		{
			if ( !(valueObj2 instanceof Short) ) return -1;
			short value1 = ((Short)valueObj1).shortValue();
			short value2 = ((Short)valueObj2).shortValue();
			if(value1<value2) return -1;
			else if(value1==value2) return 0;
			else return 1;
		}
		else if (valueObj1 instanceof String)
		{
			if ( !(valueObj2 instanceof String) ) return -1;
			String value1 = ((String)valueObj1);
			String value2 = ((String)valueObj2);
			return (value1.compareTo(value2));
		}
		else if (valueObj1 instanceof java.math.BigDecimal)
		{
			if ( !(valueObj2 instanceof java.math.BigDecimal) ) return -1;
			java.math.BigDecimal value1 = ((java.math.BigDecimal)valueObj1);
			java.math.BigDecimal value2 = ((java.math.BigDecimal)valueObj2);
			return ( (value1.compareTo(value2)) );
		}
		else if (valueObj1 instanceof Double)
		{
			if ( !(valueObj2 instanceof Double) ) return -1;
			Double value1 = ((Double)valueObj1);
			Double value2 = ((Double)valueObj2);
			return ( (value1.compareTo(value2)) );
		}
		else if (valueObj1 instanceof Float)
		{
			if ( !(valueObj2 instanceof Float) ) return -1;
			Float value1 = ((Float)valueObj1);
			Float value2 = ((Float)valueObj2);
			return ( (value1.compareTo(value2)) );
		}
		else if (valueObj1 instanceof Long)
		{
			if ( !(valueObj2 instanceof Long) ) return -1;
			Long value1 = ((Long)valueObj1);
			Long value2 = ((Long)valueObj2);
			return ( (value1.compareTo(value2)) );
		}
		else if (valueObj1 instanceof Boolean)
		{
			if ( !(valueObj2 instanceof Boolean) ) return -1;
			boolean value1 = ((Boolean)valueObj1).booleanValue();
			boolean value2 = ((Boolean)valueObj2).booleanValue();
			if( value1==value2 ) return 0;
			else return -1;
		}
		else if (valueObj1 instanceof java.sql.Date)
		{
			throw new FrameworkRuntimeException("Unspuly type!");
		}
		else
		{
			throw new FrameworkRuntimeException("Unknown value type : valueObj1=" + valueObj1);
		}
	}
}
