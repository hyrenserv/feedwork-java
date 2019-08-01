package fd.ng.web.util;

import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;
import fd.ng.web.annotation.ValueConstants;

import java.io.File;
import java.util.List;

public class FileUploadUtil {
	public static final String FILEINFO_SEPARATOR = ValueConstants.DEFAULT_NONE;

	/**
	 * 获得上传后的文件。
	 * 尽量使用本方法，不要用 getUploadedFileName 自己拼路径，因为会导致win和linux的路径分隔符不一致。
	 * 比如：/tmp\abc.txt，经过 new File 后，再取值，会被修正成： \tmp\abc.txt
	 * @param fileinfo
	 * @return
	 */
	public static File getUploadedFile(String fileinfo) {
		Validator.notEmpty(fileinfo, "args(fileinfo) must not null!");
		List<String> fileinfoArr = StringUtil.split(fileinfo, FILEINFO_SEPARATOR);
		return new File(fileinfoArr.get(0));
	}

	/**
	 * 获得上传后的文件全路径名。
	 * 最好使用 getUploadedFile() 方法。因为会导致win和linux的路径分隔符不一致。
	 * 比如：/tmp\abc.txt，经过 new File 后，再取值，会被修正成： \tmp\abc.txt
	 * @param fileinfo
	 * @return
	 */
	@Deprecated
	public static String getUploadedFileName(String fileinfo) {
		Validator.notEmpty(fileinfo, "args(fileinfo) must not null!");
		List<String> fileinfoArr = StringUtil.split(fileinfo, FILEINFO_SEPARATOR);
		return fileinfoArr.get(0);
	}

	public static String getOriginalFileName(String fileinfo) {
		Validator.notEmpty(fileinfo, "args(fileinfo) must not null!");
		List<String> fileinfoArr = StringUtil.split(fileinfo, FILEINFO_SEPARATOR);
		return fileinfoArr.get(1);
	}

	public static String getOriginalFileSize(String fileinfo) {
		Validator.notEmpty(fileinfo, "args(fileinfo) must not null!");
		List<String> fileinfoArr = StringUtil.split(fileinfo, FILEINFO_SEPARATOR);
		return fileinfoArr.get(2);
	}

	public static String getOriginalFileType(String fileinfo) {
		Validator.notEmpty(fileinfo, "args(fileinfo) must not null!");
		List<String> fileinfoArr = StringUtil.split(fileinfo, FILEINFO_SEPARATOR);
		return fileinfoArr.get(3);
	}
}
