package fd.ng.cmdtools.loadrunner;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * class=jdbc connType=conn driver=org.postgresql.Driver url=jdbc:postgresql://localhost:5432/postgres user=fd passwd=xxx123 sql="select * from test"
 */
public class JdbcTaskThread extends TaskPanel {
	public JdbcTaskThread(String taskName, Map<String, Object> forThreadData) {
		super(taskName, forThreadData);
	}

	@Override
	public String executor() throws TaskException {
		try {
			String connType = (String)forThreadData.get("connType");
			if("pool".equalsIgnoreCase(connType))
				return pool();
			else if("conn".equalsIgnoreCase(connType))
				return conn();
			else
				throw new TaskException("Unsupport conn type : " + connType);
		} catch (Exception e) {
			// 必须把自己的代码catch住，并抛出TaskException异常。
			// 基类通过捕获该异常，判断被压测的代码是否出错了。
			if( e instanceof TaskException)
				throw (TaskException)e;
			else
				throw new TaskException(e);
		}
	}

	private String conn() throws TaskException {
		String connURL=(String)forThreadData.get("url"); //
		String driverClass=(String)forThreadData.get("driver"); //
		String dbuser=(String)forThreadData.get("user"); //
		String dbpasswd=(String)forThreadData.get("passwd"); //

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName(driverClass);
			conn = DriverManager.getConnection(connURL, dbuser, dbpasswd);
			String sql = (String) forThreadData.get("sql");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			String result = "No Data in this test.";
			if (rs.next()) {
				Object obj = rs.getObject(1);
				if (obj != null)
					result = obj.toString();
			}
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			throw new TaskException(e);
		} finally {
			try{ if(rs!=null) rs.close(); }catch (Exception e){}
			try{ if(stmt!=null) stmt.close(); }catch (Exception e){}
			try{ if(conn!=null) conn.close(); }catch (Exception e){}
		}
	}

	private String pool() throws TaskException {
		return null;
	}

	public static Map<String, Object> buildInitTaskData(Map<String, String> cmdMap) {
		Map<String, Object> taskParamMap = new HashMap<>();
		taskParamMap.put("connType", cmdMap.get("connType")); // pool | conn
		taskParamMap.put("driver", cmdMap.get("driver"));
		taskParamMap.put("url", cmdMap.get("url"));
		taskParamMap.put("user", cmdMap.get("user"));
		taskParamMap.put("passwd", cmdMap.getOrDefault("passwd", ""));
		taskParamMap.put("sql", cmdMap.get("sql"));
		// 这些在主线程中构造的数据，不允许被修改了
		taskParamMap = Collections.unmodifiableMap(taskParamMap);
		return taskParamMap;
	}
}
