package fd.ng.web.hmfmswebapp.customs.listener;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.DateUtil;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.web.hmfmswebapp.a0101.UserForTestTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitListener implements ServletContextListener {
	protected static final Logger logger = LogManager.getLogger(InitListener.class.getName());
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		System.out.println("ServletContextListener contextInitialized");
		try ( DatabaseWrapper db = new DatabaseWrapper() ) {
			if (db.isExistTable(UserForTestTable.TableName))
				db.ExecDDL("drop table " + UserForTestTable.TableName);
			db.ExecDDL("create table " + UserForTestTable.TableName +
					"( name varchar(48) not null primary key, " +
					"password varchar(20), age int, create_time varchar(20))");
			int nums = SqlOperator.execute(db, "insert into " + UserForTestTable.TableName +
					" values('aoot', '11111', 80, '" + DateUtil.getDateTime() + "' )");
			if (nums != 1) {
				db.rollback();
				throw new FrameworkRuntimeException("init data for table[ " + UserForTestTable.TableName + "] failed!");
			}
			db.commit();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		System.out.println("ServletContextListener contextDestroyed");
	}
}
