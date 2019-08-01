package fd.ng.web.hmfmswebapp.upload;

import fd.ng.core.exception.BusinessSystemException;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.web.annotation.RequestParam;
import fd.ng.web.annotation.UploadFile;
import fd.ng.web.fileupload.FileItem;
import fd.ng.web.fileupload.FileUploadException;
import fd.ng.web.fileupload.disk.DiskFileItemFactory;
import fd.ng.web.fileupload.servlet.ServletFileUpload;
import fd.ng.web.hmfmswebapp.WebappBaseAction;
import fd.ng.web.util.FileUploadUtil;
import fd.ng.web.helper.HttpDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class FileUploadAction extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger(FileUploadAction.class.getName());

	public String welcome(String username, int age) {
		return "username="+username+", age="+age;
	}
	// savedDir 不需要指定，除非有什么特殊目的
	@UploadFile(savedDir = "/tmp")
	public String uploadfiles(String userid, int filelabel, String addr,
	                          String filesRoot, // 把接收到的文件保存到这个目录中
	                          String[] file_group1, @RequestParam(nullable = true) String file_group2,
	                          @RequestParam(nullable = true) String[] hiddenval) throws IOException {
		// 通过file的值，提供公共函数用于获取各种属性
		for(String f1_newFileinfo : file_group1) {
			File f1_newFile = FileUploadUtil.getUploadedFile(f1_newFileinfo);
			if(!f1_newFile.isFile()) {
				String msg = "upload file_group1["+FileUploadUtil.getUploadedFileName(f1_newFileinfo)+"] failed";
				logger.error(msg);
				return msg;
			}
			else {
				String orgnFilename = FileUploadUtil.getOriginalFileName(f1_newFileinfo);
				filesRoot = FileUtil.fixPathName(filesRoot);
				Path newFilePath = Paths.get(filesRoot+orgnFilename);
				Files.move(f1_newFile.toPath(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		if(file_group2!=null) {
			File f2_newFile = FileUploadUtil.getUploadedFile(file_group2);
			if (!f2_newFile.isFile()) {
				String msg = "upload file_group2 failed";
				logger.error(msg);
				return msg;
			} else {
				String orgnFilename = FileUploadUtil.getOriginalFileName(file_group2);
				filesRoot = FileUtil.fixPathName(filesRoot);
				Path newFilePath = Paths.get(filesRoot + orgnFilename);
				Files.move(f2_newFile.toPath(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		String ret = "userid="+userid+", addr="+addr+", filelabel="+filelabel;
		if(hiddenval!=null) ret += ", hiddenval=" + Arrays.toString(hiddenval);
		ret += ", file_group1=" + Arrays.toString(file_group1);
		if(file_group2!=null)
			ret +=	", file_group2="+file_group2
				+ ", f2_newFileName="+ FileUploadUtil.getUploadedFileName(file_group2)
				+ ", f2_orgnFileName="+ FileUploadUtil.getOriginalFileName(file_group2)
				+ ", f2_orgnFileSize="+ FileUploadUtil.getOriginalFileSize(file_group2)
				+ ", f2_orgnFileType="+ FileUploadUtil.getOriginalFileType(file_group2)
				;
		return ret;
	}

	// 自己通过 request 处理上传的文件。无特殊情况，不要使用这种编程方式
	public void uploadSelfControl() throws Exception {
		List<FileItem> fileItems = uploadDealing();

		for(FileItem item : fileItems) {
			String name = item.getFieldName();
			if (item.isFormField()) {
				String valueUTF8 = item.getString(CodecUtil.UTF8_STRING);
			} else { // 处理上传的文件
				// 注意：Windows系统上传文件，浏览器将传递该文件的完整路径，Linux系统上传文件，浏览器只传递该文件的名称
				String fileName = item.getName();
				String contentType = item.getContentType();
				boolean isInMemory = item.isInMemory();
				long sizeInBytes = item.getSize();

//				byte[] data = item.get(); // 直接得到内存中的文件
				if(true) { // 把上传的文件存储到业务上需要存储的位置
					File uploadedFile = new File("/tmp/upfiles/uploaded/"+fileName);
					item.write(uploadedFile); // 用于将FileItem对象中保存的主体内容保存到某个指定的文件中
				} else { // 直接对文件二进制内容流进行操作
					InputStream uploadedStream = item.getInputStream();
					// 各种处理
					uploadedStream.close();
				}
				item.delete(); // 清空FileItem类对象中存放的主体内容，如果主体内容被保存在临时文件中，delete方法将删除该临时文件
			}
		}
	}

	private List<FileItem> uploadDealing() {
		HttpServletRequest request = HttpDataHolder.getRequest();

		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Max Memory Size 设置是否使用临时文件保存解析出的数据的临界值（单位是字节）
		// 对于上传的字段内容，需要临时保存解析出的数据。因为内存有限，所以对于太大的数据要用临时文件来保存这些数据
		// FileItem类对象内部用了两个成员变量来分别存储提交上来的数据的描述头和主体内容
		// 当主体内容的大小小于setSizeThreshold方法设置的临界值时，主体内容将会被保存在内存中
		factory.setSizeThreshold(1024*1024);
		// setSizeThreshold的配套方法，用于设置setSizeThreshold方法中提到的临时文件的存放目录（必须使用绝对路径）
		// 如果不设置，则使用 java.io.tmpdir 环境属性所指定的目录
		// 临时文件命名规则： upload_00000005（八位或八位以上的数字）.tmp
		factory.setRepository(new File("/tmp/upfiles/temp"));
//		factory.setDefaultCharset("UTF-8");

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setHeaderEncoding(CodecUtil.UTF8_STRING); // 解决中文路径或文件名乱码？
		// Set overall request size constraint
		upload.setSizeMax(100*1024); // Max Request Size, 上传文件允许的大小

		// Parse the request
		try {
			return upload.parseRequest(request);
		} catch (FileUploadException e) {
			throw new BusinessSystemException(e);
		}
	}
}
