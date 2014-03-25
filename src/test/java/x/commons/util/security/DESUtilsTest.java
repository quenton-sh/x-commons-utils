package x.commons.util.security;

import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import x.commons.util.security.DESUtils;
import x.commons.util.security.DESUtils.Transformation;


public class DESUtilsTest {
	
	private static final byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
	private static final byte[] key = "12345678".getBytes();
	private static final String plainStr = "commons-utils-desutils-test";
	private static final byte[] plain = plainStr.getBytes();

	@Test
	public void generateKey() throws Exception {
		byte[] key = DESUtils.generateKey();
		assertEquals(8, key.length);
	}
	
	@Test
	public void encrypt() throws Exception {
		byte[] cipher = DESUtils.encrypt(plain, key, Transformation.DES_CBC_PKCS5Padding, iv);
		String s = Hex.encodeHexString(cipher);
//		System.out.println(s);
		assertEquals("cba9a1b374d90ef948b4db6ca12cb89d6c15e7bd5b65605766e127ab36bfce0e", s);
	}
	
	@Test
	public void encryptDefault() throws Exception {
		byte[] cipher = DESUtils.encrypt(plain, key);
		String s = Hex.encodeHexString(cipher);
//		System.out.println(s);
		assertEquals("60b72475e7dcd1bba3ce11e487ecb367024090046f0faacda52da477662201f5", s);
	}
	
	@Test
	public void decrypt() throws Exception {
		byte[] cipher = Hex.decodeHex("cba9a1b374d90ef948b4db6ca12cb89d6c15e7bd5b65605766e127ab36bfce0e".toCharArray());
		byte[] plain = DESUtils.decrypt(cipher, key, Transformation.DES_CBC_PKCS5Padding, iv);
		assertEquals(plainStr, new String(plain));
	}
	
	@Test
	public void decryptDefault() throws Exception {
		byte[] cipher = Hex.decodeHex("60b72475e7dcd1bba3ce11e487ecb367024090046f0faacda52da477662201f5".toCharArray());
		byte[] plain = DESUtils.decrypt(cipher, key);
		assertEquals(plainStr, new String(plain));
	}
}
