package fd.ng.web.hmfmswebapp.upload;

import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.netclient.http.SubmitMediaType;
import fd.ng.web.WebBaseTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileUploadActionTest extends WebBaseTestCase {
	@Test
	public void uploadfiles() throws IOException {
		String tempDir = FileUtil.TEMP_DIR_NAME + "uploadfiles" + FileUtil.PATH_SEPARATOR_CHAR;
		if(!Paths.get(tempDir).toFile().exists()) Files.createDirectory(Paths.get(tempDir));

		String file_group1_file1 = tempDir+"中文文件.txt";
		FileUtil.createFileIfAbsent(file_group1_file1, DateUtil.getDateTime()+"\n脸上的肌肤\n\t234 sdf ...");
		String file_group1_file2 = tempDir+"shell.sh";
		FileUtil.createFileIfAbsent(file_group1_file2, DateUtil.getDateTime()+" shell");
		String file_group2_file1 = tempDir+"programe.exe";
		FileUtil.createFileIfAbsent(file_group2_file1, DateUtil.getDateTime()+" exe");

		String filesRoot = "d:\\tmp\\upfiles\\uploaded";
		String responseValue = new HttpClient(SubmitMediaType.MULTIPART)
				.addData("filesRoot", filesRoot)
				.addData("userid", "u1").addData("addr", "北京路222号 A座").addData("filelabel", "100")
				.addData("hiddenval", new String[]{"数组 h1", "数组 h2", "数组 h3"})
				.addFile("file_group1", new String[]{file_group1_file1, file_group1_file2})
				.addFile("file_group2", file_group2_file1)
				.post(getActionUrl("uploadfiles"))
				.getBodyString();
		assertThat(responseValue, containsString("userid=u1, addr=北京路222号 A座, filelabel=100, hiddenval=[数组 h1, 数组 h2, 数组 h3], "));
		String decodeAr = CodecUtil.decodeURL(responseValue);
		assertThat(decodeAr, containsString("中文文件.txt"));
		assertThat(responseValue, containsString("shell.sh"));
		assertThat(responseValue, containsString("programe.exe"));

		File file1 = new File(filesRoot+"\\shell.sh");
		assertThat(file1.isFile(), is(true));
		file1.delete();

		File file2 = new File(filesRoot+"\\programe.exe");
		assertThat(file2.isFile(), is(true));
		file2.delete();

//		File cnFile = new File(filesRoot+"\\中文文件.txt");
//		assertThat(cnFile.isFile(), is(true));
//		cnFile.delete();
		File cnFile1 = new File(filesRoot+"\\"+CodecUtil.encodeURL("中文文件")+".txt");
		assertThat(cnFile1.isFile(), is(true));
		cnFile1.delete();
	}
}
