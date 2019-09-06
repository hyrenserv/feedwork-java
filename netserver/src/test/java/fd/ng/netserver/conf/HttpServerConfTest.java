package fd.ng.netserver.conf;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpServerConfTest {

	@Ignore("令 port/httpsPort/webContext 重复，手工执行，看是否提示了正确的警告")
	@Test
	public void testDuplicateConf() {
		HttpServerConf.getHttpServer();
	}

	@Test
	public void defaultConf() {
		assertThat(HttpServerConf.confBean.getHost(), is("localhost"));
		assertThat(HttpServerConf.confBean.getIdleTimeout(), nullValue());
		assertThat(HttpServerConf.confBean.getHttpPort(), is(8080));
		assertThat(HttpServerConf.confBean.getHttpsPort(), is(38443));
		assertThat(HttpServerConf.confBean.getWebContext(), is("/fdwebtest"));
		assertThat(HttpServerConf.confBean.getActionPattern(), is("/action/*"));
		assertThat(HttpServerConf.confBean.getSession_MaxAge(), is(28800));
		assertThat(HttpServerConf.confBean.isSession_HttpOnly(), is(false));

		HttpServerConfBean oneServer = HttpServerConf.getHttpServer(HttpServerConf.DEFAULT_DBNAME);
		assertThat(HttpServerConf.confBean.getHost(), is(oneServer.getHost()));
		assertThat(HttpServerConf.confBean.getIdleTimeout(), is(oneServer.getIdleTimeout()));
		assertThat(HttpServerConf.confBean.getHttpPort(), is(oneServer.getHttpPort()));
		assertThat(HttpServerConf.confBean.getHttpsPort(), is(oneServer.getHttpsPort()));
		assertThat(HttpServerConf.confBean.getWebContext(), is(oneServer.getWebContext()));
		assertThat(HttpServerConf.confBean.getActionPattern(), is(oneServer.getActionPattern()));
		assertThat(HttpServerConf.confBean.getSession_MaxAge(), is(oneServer.getSession_MaxAge()));
		assertThat(HttpServerConf.confBean.isSession_HttpOnly(), is(oneServer.isSession_HttpOnly()));
	}

	@Test
	public void otherConf() {
		HttpServerConfBean oneServer = HttpServerConf.getHttpServer("other");
		assertThat(oneServer.getHost(), is("localhost-1"));
		assertThat(oneServer.getIdleTimeout(), is(30001));
		assertThat(oneServer.getHttpPort(), is(8081));
		assertThat(oneServer.getHttpsPort(), is(38444));
		assertThat(oneServer.getWebContext(), is("/fdwebtest-1"));
		assertThat(oneServer.getActionPattern(), is("/action/*-1"));
		assertThat(oneServer.getSession_MaxAge(), is(28801));
		assertThat(oneServer.isSession_HttpOnly(), is(true));
	}

	@Test
	public void allDefaultConf() {
		HttpServerConfBean oneServer = HttpServerConf.getHttpServer("alldefault");
		assertThat(oneServer.getHost(), nullValue());
		assertThat(oneServer.getIdleTimeout(), nullValue());
		assertThat(oneServer.getHttpPort(), is(32102));
		assertThat(oneServer.getHttpsPort(), nullValue());
		assertThat(oneServer.getWebContext(), is("/fdctx2"));
		assertThat(oneServer.getActionPattern(), is("/action2/*"));
		assertThat(oneServer.getSession_MaxAge(), is(300));
		assertThat(oneServer.isSession_HttpOnly(), is(false));
	}
}