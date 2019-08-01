package fd.ng.web.fileupload.util.mime;

import fd.ng.core.utils.CodecUtil;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class Base64DecoderTest {
	private static final String US_ASCII_CHARSET = "US-ASCII";

	@Test
	public void rfc4648Section10Decode() throws Exception {
		assertEncoded("", "");
		assertEncoded("f", "Zg==");
		String ed = CodecUtil.encodeBASE64("f");
		assertEncoded("fo", "Zm8=");
		assertEncoded("foo", "Zm9v");
		assertEncoded("foob", "Zm9vYg==");
		assertEncoded("fooba", "Zm9vYmE=");
		assertEncoded("foobar", "Zm9vYmFy");
	}

	/**
	 * Test our decode with pad character in the middle.
	 * Continues provided that the padding is in the correct place,
	 * i.e. concatenated valid strings decode OK.
	 */
	@Test
	public void decodeWithInnerPad() throws Exception {
		assertEncoded("Hello WorldHello World", "SGVsbG8gV29ybGQ=SGVsbG8gV29ybGQ=");
	}

	/**
	 * Ignores non-BASE64 bytes.
	 */
	@Test
	public void nonBase64Bytes() throws Exception {
		assertEncoded("Hello World", "S?G!V%sbG 8g\rV\t\n29ybGQ*=");
	}

	@Test(expected = IOException.class)
	public void truncatedString() throws Exception {
		final byte[] x = new byte[]{'n'};
		Base64Decoder.decode(x, new ByteArrayOutputStream());
	}

	@Test
	public void decodeTrailingJunk() throws Exception {
		assertEncoded("foobar", "Zm9vYmFy!!!");
	}

	// If there are valid trailing Base64 chars, complain
	@Test
	public void decodeTrailing1() throws Exception {
		assertIOException("truncated", "Zm9vYmFy1");
	}

	// If there are valid trailing Base64 chars, complain
	@Test
	public void decodeTrailing2() throws Exception {
		assertIOException("truncated", "Zm9vYmFy12");
	}

	// If there are valid trailing Base64 chars, complain
	@Test
	public void decodeTrailing3() throws Exception {
		assertIOException("truncated", "Zm9vYmFy123");
	}

	@Test
	public void badPadding() throws Exception {
		assertIOException("incorrect padding, 4th byte", "Zg=a");
	}

	@Test
	public void badPaddingLeading1() throws Exception {
		assertIOException("incorrect padding, first two bytes cannot be padding", "=A==");
	}

	@Test
	public void badPaddingLeading2() throws Exception {
		assertIOException("incorrect padding, first two bytes cannot be padding", "====");
	}

	// This input causes java.lang.ArrayIndexOutOfBoundsException: 1
	// in the Java 6 method DatatypeConverter.parseBase64Binary(String)
	// currently reported as truncated (the last chunk consists just of '=')
	@Test
	public void badLength() throws Exception {
		assertIOException("truncated", "Zm8==");
	}

	// These inputs cause java.lang.ArrayIndexOutOfBoundsException
	// in the Java 6 method DatatypeConverter.parseBase64Binary(String)
	// The non-ASCII characters should just be ignored
	@Test
	public void nonASCIIcharacter() throws Exception {
		assertEncoded("f","Zg=�="); // A-grave
		assertEncoded("f","Zg=\u0100=");
	}

	private static void assertEncoded(String clearText, String encoded) throws Exception {
		byte[] expected = clearText.getBytes(US_ASCII_CHARSET);

		ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
		byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
		Base64Decoder.decode(encodedData, out);
		byte[] actualByFileuploadUtil = out.toByteArray();
		String decodeStrByFileuploadUtil = new String(actualByFileuploadUtil);

		assertArrayEquals(expected, actualByFileuploadUtil);

//		String decodeStrByJava8 = CodecUtil.decodeBASE64(encoded);
//		byte[] actualByJava8 = decodeStrByJava8.getBytes(US_ASCII_CHARSET);
//
//		assertArrayEquals(expected, actualByJava8);
	}

	private static void assertIOException(String messageText, String encoded) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
		byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
		try {
			Base64Decoder.decode(encodedData, out);
			fail("Expected IOException");
		} catch (IOException e) {
			String em = e.getMessage();
			assertTrue("Expected to find " + messageText + " in '" + em + "'",em.contains(messageText));
		}
	}
}