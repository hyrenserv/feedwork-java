package fd.ng.core.utils;

import fd.ng.test.junit.FdBaseTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileUtilTest extends FdBaseTestCase {
	@Test
	public void deleteDirectoryFilesAndDirectory() throws IOException {
		String testDeleteDir_RootDir = "/tmp/xxx/deleteTest/" + "xxx";
		String curDir = testDeleteDir_RootDir;
		Files.createDirectories(Paths.get(curDir));
		boolean ok = FileUtil.createOrReplaceFile(curDir + "/d0.txt", "d0.file");
		assertThat(ok, is(true));

		curDir = testDeleteDir_RootDir+"/d11";
		Files.createDirectories(Paths.get(curDir));
		ok = FileUtil.createOrReplaceFile(curDir+"/d11.txt", "d11.file");
		assertThat(ok, is(true));
		curDir = testDeleteDir_RootDir+"/d12";
		Files.createDirectories(Paths.get(curDir));
		ok = FileUtil.createOrReplaceFile(curDir+"/d12.txt", "d12.file");
		assertThat(ok, is(true));

		curDir = testDeleteDir_RootDir+"/d11/d11_1";
		Files.createDirectories(Paths.get(curDir));
		ok = FileUtil.createOrReplaceFile(curDir+"/d11_1.txt", "d11_1.file");
		assertThat(ok, is(true));

		ok = FileUtil.deleteDirectoryFiles(testDeleteDir_RootDir);
		assertThat(ok, is(true));
		assertThat(Files.isDirectory(Paths.get(testDeleteDir_RootDir+"/d11/d11_1")), is(true));
		assertThat(Files.exists(Paths.get(testDeleteDir_RootDir+"/d11/d11_1/d11_1.txt")), is(false));
		assertThat(Files.exists(Paths.get(testDeleteDir_RootDir+"/d11/d11.txt")), is(false));
		assertThat(Files.isDirectory(Paths.get(testDeleteDir_RootDir+"/d12")), is(true));
		assertThat(Files.exists(Paths.get(testDeleteDir_RootDir+"/d12/d12.txt")), is(false));
		assertThat(Files.exists(Paths.get(testDeleteDir_RootDir+"/d0.txt")), is(false));

		FileUtil.deleteDirectory(new File(testDeleteDir_RootDir));
		assertThat(Files.isDirectory(Paths.get(testDeleteDir_RootDir)), is(false));
	}
}
