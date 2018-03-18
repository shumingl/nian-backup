package so.nian.backup.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Base64Util {

	public static String encode(byte[] input) {
		return Base64.encodeBase64String(input);
	}

	public static byte[] decode(String inputString) throws IOException {
		return Base64.decodeBase64(inputString);
	}

	public static String encode(String inputString, String encode) throws UnsupportedEncodingException {
		return Base64.encodeBase64String(inputString.getBytes(encode));
	}

	public static String decode(String inputString, String encode) throws IOException {
		return new String(Base64.decodeBase64(inputString.getBytes(encode)), encode);
	}
}
