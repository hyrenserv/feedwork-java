package fd.ng.netclient;

import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.netclient.conf.NetclientConf;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netclient.http.SubmitMediaType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;

public class HttpClientTest {
	@Test
	public void readConf() {
		// 需要保证netclientinfo.conf中，每个值都设置成下面了
		assertThat(NetclientConf.ConnectTimeout, is(250L));
		assertThat(NetclientConf.ReadTimeout, is(251L));
		assertThat(NetclientConf.WriteTimeout, is(252L));
		assertThat(NetclientConf.RetryOnConnectionFailure, is(false));
		assertThat(NetclientConf.HasCookie, is(false));
	}

	// 以下测试，要启动 web 模块里面的 测试WEB
	@Test
	public void postSession() {
		HttpClient.ResponseValue resValNoLogin = new HttpClient().post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/login/dologin/checkSession");
		assertThat(resValNoLogin.getBodyString(), containsString("No Session Data"));

		HttpClient.ResponseValue resValLogin = new HttpClient()
				.buildSession()
				.addData("username", "admin")
				.addData("password", "admin")
				.post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/login/dologin/loginAtSession");
		assertThat(resValLogin.getCode(), is(200));

		HttpClient.ResponseValue resVal = new HttpClient().post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/login/dologin/checkSession");
		assertThat(resVal.getBodyString(), containsString("way"));
		assertThat(resVal.getBodyString(), containsString("session@user:admin"));
		assertThat(resVal.getBodyString(), containsString("intNull=null"));
	}

	@Test
	public void postJson() {
		HttpClient.ResponseValue resVal = new HttpClient(SubmitMediaType.JSON)
				.addData("name", "x-xx xx")
				.addData("age", 239865)
				.post(
				"http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/json/welcome");
		assertThat(resVal.getBodyString(), allOf(containsString("x-xx xx"), containsString("239865")));

		UserInHttpClient user = new UserInHttpClient("yyy 水电费", 100088);
		resVal = new HttpClient(SubmitMediaType.JSON)
				.addJson(JsonUtil.toJson(user))
				.post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/json/welcome");
		assertThat(resVal.getBodyString(), allOf(containsString("yyy 水电费"), containsString("100088")));
	}

	@Test
	public void upload() throws IOException {
		HttpClient httpClient = new HttpClient()
				.addData("username", "xxx xx")
				.addData("age", "23");
		HttpClient.ResponseValue resVal = httpClient.post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/upload/welcome");
		assertThat(resVal.getBodyString(), containsString("username=xxx xx, age=23"));

		String tempDir = FileUtil.TEMP_DIR_NAME + "uploadfiles" + FileUtil.PATH_SEPARATOR_CHAR;
		if(!Paths.get(tempDir).toFile().exists()) Files.createDirectory(Paths.get(tempDir));

		String file1 = tempDir+"中文文件.txt";
		FileUtil.createFileIfAbsent(file1, DateUtil.getDateTime()+"\n脸上的肌肤\n\t234 sdf ...");
		String file2 = tempDir+"shell.sh";
		FileUtil.createFileIfAbsent(file2, DateUtil.getDateTime()+" shell");

		String filesRoot = "d:\\tmp\\upfiles\\uploaded";
		resVal = httpClient.reset(SubmitMediaType.MULTIPART)
				.addData("userid", "u1")
				.addData("addr", "北京路222号 A座")
				.addData("filelabel", "333")
				.addData("filesRoot", filesRoot)
				.addFile("file_group1", file1)
				.addFile("file_group1", file2)
				.post("http://localhost:8080/fdwebtest/action/fd/ng/web/hmfmswebapp/upload/uploadfiles");
		assertThat(resVal.getBodyString(), containsString("userid=u1, addr=北京路222号 A座"));

		File cnFile = new File(filesRoot+"\\"+ CodecUtil.encodeURL("中文文件")+".txt");
		assertThat(cnFile.isFile(), is(true));
		cnFile.delete();
		File file = new File(filesRoot+"\\shell.sh");
		assertThat(file.isFile(), is(true));
		file.delete();
	}

	public static class UserInHttpClient {
		private String  name;
		private int     age;

		public UserInHttpClient(final String name, final int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(final int age) {
			this.age = age;
		}
	}
}