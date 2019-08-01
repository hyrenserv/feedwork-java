package fd.ng.core.exception.beans;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;

import java.sql.SQLException;

public class TwoClass {
	public void dealCompute(String thrType) {
		System.out.println("dealCompute ...");
		try {
			OneClass one = new OneClass();
			one.compute();
		} catch (Exception e) {
			if("onlyMessage".equalsIgnoreCase(thrType))
				throw new RuntimeException("onlyMessage");
			else if("Message_Ex".equalsIgnoreCase(thrType))
				throw new RuntimeException("Message_Ex", e);
			else if("onlyEx".equalsIgnoreCase(thrType))
				throw new RuntimeException(e);
		}
	}

	public void dealExecSQL(String thrType) {
		System.out.println("dealExecSQL ...");
		try {
			OneClass one = new OneClass();
			one.execSQL();
		} catch (SQLException se) {
			se.printStackTrace();
			System.out.println("================================= ire1:");
			RawlayerRuntimeException ire1 = new RawlayerRuntimeException("onlyMessage");
			System.out.println("================================= ire2:");
			RawlayerRuntimeException ire2 = new RawlayerRuntimeException("Message_Ex", se);
			System.out.println("================================= ire3:");
			RawlayerRuntimeException ire3 = new RawlayerRuntimeException(se);
			System.out.println("=================================");
//			SQLException e = new SQLException("新的错误提示信息", se.getSQLState(),
//					se.getErrorCode());
			//e.setNextException(se);
			if("onlyMessage".equalsIgnoreCase(thrType))
				throw ire1;
			else if("Message_Ex".equalsIgnoreCase(thrType)) {
				throw ire2;
			} else if("onlyEx".equalsIgnoreCase(thrType))
				throw ire3;
		}
	}

	public void dealFramework(String thrType) {
		System.out.println("dealExecSQL ...");
		try {
			OneClass one = new OneClass();
			one.execSQL();
		} catch (SQLException se) {
			se.printStackTrace();
			System.out.println("================================= ire1:");
			FrameworkRuntimeException ire1 = new FrameworkRuntimeException("onlyMessage");
			System.out.println("================================= ire2:");
			FrameworkRuntimeException ire2 = new FrameworkRuntimeException("Message_Ex", se);
			System.out.println("================================= ire3:");
			FrameworkRuntimeException ire3 = new FrameworkRuntimeException(se);
			System.out.println("=================================");
//			SQLException e = new SQLException("新的错误提示信息", se.getSQLState(),
//					se.getErrorCode());
			//e.setNextException(se);
			if("onlyMessage".equalsIgnoreCase(thrType))
				throw ire1;
			else if("Message_Ex".equalsIgnoreCase(thrType)) {
				throw ire2;
			} else if("onlyEx".equalsIgnoreCase(thrType))
				throw ire3;
		}
	}
}
