package fd.ng.core.utils;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.exception.internal.RuntimeOnlyMessageException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CodecUtil {
	public static final String  GBK_STRING    = "GBK";
	public static final Charset GBK_CHARSET   = Charset.forName("GBK");
	public static final String  UTF8_STRING   = "UTF-8";
	public static final Charset UTF8_CHARSET  = StandardCharsets.UTF_8;

	/**
	 * 将 URL 编码
	 */
	public static String encodeURL(String str) {
		String target;
		try {
			target = URLEncoder.encode(str, UTF8_STRING);
		} catch (UnsupportedEncodingException e) {
			throw new RawlayerRuntimeException("encode : " + str, e);
		}
		return target;
	}

	/**
	 * 将 URL 解码
	 */
	public static String decodeURL(String str) {
		String target;
		try {
			target = URLDecoder.decode(str, UTF8_STRING);
		} catch (UnsupportedEncodingException e) {
			throw new RawlayerRuntimeException("decode : " + str, e);
		}
		return target;
	}

	/**
	 * 将字符串 Base64 编码
	 */
	public static String encodeBASE64(String str) {
		String target;
		try {
			target = Base64.getEncoder().encodeToString(str.getBytes(UTF8_STRING));
		} catch (UnsupportedEncodingException e) {
			throw new FrameworkRuntimeException("encode : " + str, e);
		}
		return target;
	}

	/**
	 * 将字符串 Base64 解码
	 */
	public static String decodeBASE64(String str) {
		String target;
		try {
			target = new String(Base64.getDecoder().decode(str) , UTF8_STRING);
		} catch (UnsupportedEncodingException e) {
			throw new FrameworkRuntimeException("encode : " + str, e);
		}
		return target;
	}
}
