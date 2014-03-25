package x.commons.util;

import java.util.zip.CRC32;

public class HashUtils {

	public static String crc32(byte[] data) throws Exception {
		CRC32 crc = new CRC32();
		crc.update(data);
		return Long.toHexString(crc.getValue());
	}

	public static byte[] md5(byte[] data) throws Exception {
		java.security.MessageDigest md = java.security.MessageDigest
				.getInstance("MD5");
		md.update(data);
		return md.digest();
	}

	public static byte[] sha1(byte[] data) throws Exception {
		java.security.MessageDigest md = java.security.MessageDigest
				.getInstance("SHA-1");
		md.update(data);
		return md.digest();
	}

}
