package x.commons.util.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESUtils {
	
	public static enum Transformation {
		DEFAULT("DES"), 
		DES_CBC_NoPadding("DES/CBC/NoPadding"), 
		DES_CBC_PKCS5Padding("DES/CBC/PKCS5Padding"),
		DES_ECB_NoPadding("DES/ECB/NoPadding"),
		DES_ECB_PKCS5Padding("DES/ECB/PKCS5Padding");
		
		private final String value;
		private Transformation(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}

	private static final String ALG = "DES";

	public static byte[] generateKey() {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		keyGen.init(56);
		return keyGen.generateKey().getEncoded();
	}
	
	public static byte[] encrypt(byte[] plainBytes, byte[] secretKey)
			throws Exception {
		return encrypt(plainBytes, secretKey, Transformation.DEFAULT, null);
	}
	
	public static byte[] encrypt(byte[] plainBytes, byte[] secretKey, Transformation transformation, byte[] iv)
			throws Exception {
		DESKeySpec skeySpec = new DESKeySpec(secretKey);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALG);
		SecretKey secretKeyObj = keyFactory.generateSecret(skeySpec);
		Cipher cipher = Cipher.getInstance(transformation.value);
		if (iv != null) {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeyObj, ivSpec);
		} else {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeyObj);
		}
		return cipher.doFinal(plainBytes);
	}
	
	public static byte[] decrypt(byte[] cipherBytes, byte[] secretKey)
			throws Exception {
		return decrypt(cipherBytes, secretKey, Transformation.DEFAULT, null);
	}

	public static byte[] decrypt(byte[] cipherBytes, byte[] secretKey, Transformation transformation, byte[] iv)
			throws Exception {
		DESKeySpec skeySpec = new DESKeySpec(secretKey);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALG);
		SecretKey secretKeyObj = keyFactory.generateSecret(skeySpec);
		Cipher cipher = Cipher.getInstance(transformation.value);
		if (iv != null) {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKeyObj, ivSpec);
		} else {
			cipher.init(Cipher.DECRYPT_MODE, secretKeyObj);
		}
		return cipher.doFinal(cipherBytes);
	}

}
