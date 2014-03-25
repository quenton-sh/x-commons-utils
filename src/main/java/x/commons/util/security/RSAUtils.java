package x.commons.util.security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtils {
	
	public static enum Transformation {
		DEFAULT("RSA"), 
		RSA_ECB_PKCS1Padding("RSA/ECB/PKCS1Padding"),
		RSA_ECB_OAEPWithSHA1AndMGF1Padding("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"),
		RSA_ECB_OAEPWithSHA256AndMGF1Padding("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		
		private final String value;
		private Transformation(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}

	private static final String ALG = "RSA";

	public static KeyPair generateKeyPair() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALG);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		keyGen.initialize(1024);
		return keyGen.generateKeyPair();
	}

	public static Key buildPrivateKey(byte[] keyData) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALG);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	public static Key buildPublicKey(byte[] keyData) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALG);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}
	
	public static byte[] encrypt(byte[] plainBytes, Key key)
			throws Exception {
		return encrypt(plainBytes, key, Transformation.DEFAULT);
	}

	public static byte[] encrypt(byte[] bytes, Key key, Transformation transformation)
			throws Exception {
		Cipher cipher = Cipher.getInstance(transformation.value);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(bytes);
	}
	
	public static byte[] decrypt(byte[] cipherBytes, Key key)
			throws Exception {
		return decrypt(cipherBytes, key, Transformation.DEFAULT);
	} 

	public static byte[] decrypt(byte[] bytes, Key key, Transformation transformation)
			throws Exception {
		Cipher cipher = Cipher.getInstance(transformation.value);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(bytes);
	}
}