package x.commons.util.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

	public static enum Transformation {
		DEFAULT("AES"), 
		AES_CBC_NoPadding("AES/CBC/NoPadding"), 
		AES_CBC_PKCS5Padding("AES/CBC/PKCS5Padding"),
		AES_ECB_NoPadding("AES/ECB/NoPadding"),
		AES_ECB_PKCS5Padding("AES/ECB/PKCS5Padding");
		
		private final String value;
		private Transformation(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}

	private static final String ALG = "AES";
	
	public static byte[] generateKey() {
		return generateKey(128);
	}

	public static byte[] generateKey(int keyLen) {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		keyGen.init(keyLen);
		return keyGen.generateKey().getEncoded();
	}
	
	public static byte[] encrypt(byte[] plainBytes, byte[] secretKey)
			throws Exception {
		return encrypt(plainBytes, secretKey, Transformation.DEFAULT, null);
	}
	
	public static byte[] encrypt(byte[] plainBytes, byte[] secretKey, Transformation transformation, byte[] iv)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(secretKey, ALG);
		Cipher cipher = Cipher.getInstance(transformation.value);
		if (iv != null) {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
		} else {
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		}
		return cipher.doFinal(plainBytes);
	}
	
	public static byte[] decrypt(byte[] cipherBytes, byte[] secretKey)
			throws Exception {
		return decrypt(cipherBytes, secretKey, Transformation.DEFAULT, null);
	}

	public static byte[] decrypt(byte[] cipherBytes, byte[] secretKey, Transformation transformation, byte[] iv)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(secretKey, ALG);
		Cipher cipher = Cipher.getInstance(transformation.value);
		if (iv != null) {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
		} else {
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		}
		return cipher.doFinal(cipherBytes);
	}
}
