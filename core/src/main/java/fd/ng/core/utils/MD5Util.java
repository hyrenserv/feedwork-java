package fd.ng.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @program: feedwork
 * @description: MD5加密
 * @author: xchao
 * @create: 2019-10-11 10:28
 */
public class MD5Util {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * 计算一个字符串的md5
	 *
	 * @param src 原字符串
	 * @return 计算md5返回
	 */
	public static String md5String(String src) {

		StringBuilder sb = new StringBuilder(32);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(src.getBytes(CodecUtil.UTF8_CHARSET));
			for (int i = 0; i < array.length; i++) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
			}
			return sb.toString();
		} catch (Exception e) {
			logger.error("MD5 mart failed"+src, e);
			return null;
		}
	}

	/**
	 * 计算文件级别的md5
	 * @param fileName
	 * @return
	 */
	public static String md5File(String fileName) {

		return md5File(new File(fileName));
	}

	/**
	 * 计算文件级别的md5
	 * @param file
	 * @return
	 */
	public static String md5File(File file) {
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int len;
			MessageDigest md = MessageDigest.getInstance("MD5");
			while ((len = fis.read(buffer)) != -1) {
				md.update(buffer, 0, len);
			}
			byte[] b = md.digest();
			return new BigInteger(1, b).toString(16);
		} catch (NoSuchAlgorithmException e) {
			logger.error("MD5 mart failed NoSuchAlgorithmException", e);
			return null;
		} catch (IOException e) {
			logger.error("MD5 mart failed IOException", e);
			return null;
		}
	}
}
