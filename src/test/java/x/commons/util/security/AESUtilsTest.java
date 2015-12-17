package x.commons.util.security;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import x.commons.util.security.AESUtils.Transformation;

public class AESUtilsTest {

	private static final byte[] iv = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
			0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
	private static final byte[] key = "0123456789ABCDEF".getBytes();
	private static final String plainStr = "commons-utils-aesutils-test";
	private static final byte[] plain = plainStr.getBytes();

	@Test
	public void generateKey() throws Exception {
		byte[] key = AESUtils.generateKey();
		assertEquals(16, key.length);
		
		key = AESUtils.generateKey(256);
		assertEquals(32, key.length);
		
		try {
			AESUtils.generateKey(123);
			fail();
		} catch (InvalidParameterException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void encrypt() throws Exception {
		byte[] cipher = AESUtils.encrypt(plain, key, Transformation.AES_CBC_PKCS5Padding, iv);
		String s = Hex.encodeHexString(cipher);
//		System.out.println(s);
		assertEquals("4b4370aaa9eb5c410410fabb0ea5700038747e906d982e26bfbf15ea63452821", s);
	}
	
	@Test
	public void encryptDefault() throws Exception {
		byte[] cipher = AESUtils.encrypt(plain, key);
		String s = Hex.encodeHexString(cipher);
//		System.out.println(s);
		assertEquals("e0415f7c7cebb2bc5aaa4271f67e7f7823d6d6f571d6d108765aee48a476dc4d", s);
	}
	
	@Test
	public void decrypt() throws Exception {
		byte[] cipher = Hex.decodeHex("4b4370aaa9eb5c410410fabb0ea5700038747e906d982e26bfbf15ea63452821".toCharArray());
		byte[] plain = AESUtils.decrypt(cipher, key, Transformation.AES_CBC_PKCS5Padding, iv);
		assertEquals(plainStr, new String(plain));
	}
	
	@Test
	public void decryptDefault() throws Exception {
		byte[] cipher = Hex.decodeHex("e0415f7c7cebb2bc5aaa4271f67e7f7823d6d6f571d6d108765aee48a476dc4d".toCharArray());
		byte[] plain = AESUtils.decrypt(cipher, key);
		assertEquals(plainStr, new String(plain));
	}
}
