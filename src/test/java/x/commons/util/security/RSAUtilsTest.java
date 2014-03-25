package x.commons.util.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import x.commons.util.security.RSAUtils;
import x.commons.util.security.RSAUtils.Transformation;


public class RSAUtilsTest {

	private static final String oriStr = "commons-utils-rsautils-test";
	private static final byte[] ori = oriStr.getBytes();
	private static final String pubKeyStr = "30819f300d06092a864886f70d010101050003818d00308189028181009ae008772f141ab1973d49f0df004bdb53e863438352a00df805b04b82ac3559561b13c290eeb2f7db95c00fbda436ea9271dc0f9fdc949a920a06a3b60bf22ebe6be57e0e1df7d76c6d3ac34e9d8b63c3214371c049a910db471921e6dd9d5208066c10ffb974b752d59c3d7117f2d15ae9aafb0741547d3441fea45e27700d0203010001";
	private static final String priKeyStr = "30820277020100300d06092a864886f70d0101010500048202613082025d020100028181009ae008772f141ab1973d49f0df004bdb53e863438352a00df805b04b82ac3559561b13c290eeb2f7db95c00fbda436ea9271dc0f9fdc949a920a06a3b60bf22ebe6be57e0e1df7d76c6d3ac34e9d8b63c3214371c049a910db471921e6dd9d5208066c10ffb974b752d59c3d7117f2d15ae9aafb0741547d3441fea45e27700d02030100010281810099cb8f9c8b2bb3b657318d939c5f76f4be462f0c840430dcae4737e244492e5120a892decb7dbe4b53cb498658141254ae4852f7511082a15c1488b359bc5329728aef911bea50fb09aa207648e7e08ac0b62e5abbcc74ba23cd5689e48861ba92fed42f2fd077a961f67dfd8346e4f6ae615c97533b7f997a36c4e4f0d55121024100f92efe64e1ee66a165f841c4f400cd4651213a093da813431842fd2ea5e4b50b029f9ce55a8839c0dda88a789ba10bfe35650d03ea2e6b423cacf159046426470241009f1c9b7666c47d95b640f231d4f079d128dcd39fcaf336adb96b89d7c4f1b531119316dd55b3fc6528bd0a4375c000cbd5256968cfc6661c6f62339be37e5d0b02403143ce3de9c1357f4166c4b208bfc3d5dc2262940321a0b54a2dac5ab5a7b2f77b4c4d3bb71b69b7acdb2e8d4bcf9a4c6708147baaa804c2b2b64a7d1bf624ef024046734a3aa0c23bc10053bd6967199b368b5b4bb0a3121191d659d14ce27d0b77508aca42f261a14dc13e02bf5ff1c5e87ad5f7d85ab7441b2f0e523fa1f91f85024100b1c2d7a1bbee9c40f9da459768fd7106bda8ff485e8f4f3c548ef14c4815deaa7d28c3d43ae108432fad245e9d69dbd4a87ee499a3635fca9f3b369c241e999f";

	@Test
	public void generatorKeyPair() throws Exception {
		KeyPair kp = RSAUtils.generateKeyPair();
		Key pubk = kp.getPublic();
		Key prik = kp.getPrivate();

		assertEquals("RSA", pubk.getAlgorithm());
		assertEquals("X.509", pubk.getFormat());

		assertEquals("RSA", prik.getAlgorithm());
		assertEquals("PKCS#8", prik.getFormat());
	}

	@Test
	public void buildPublicKey() throws Exception {
		KeyPair kp = RSAUtils.generateKeyPair();
		Key pubk = kp.getPublic();
		byte[] data = pubk.getEncoded();

		Key result = RSAUtils.buildPublicKey(data);
		assertEquals(pubk, result);
	}

	@Test
	public void buildPrivateKey() throws Exception {
		KeyPair kp = RSAUtils.generateKeyPair();
		Key prik = kp.getPrivate();
		byte[] data = prik.getEncoded();

		Key result = RSAUtils.buildPrivateKey(data);
		assertEquals(prik, result);
	}

	@Test
	public void encrypt_decrypt_default() throws Exception {
		byte[] pubKey = Hex.decodeHex(pubKeyStr.toCharArray());
		byte[] priKey = Hex.decodeHex(priKeyStr.toCharArray());

		// 私钥加密
		byte[] cipher = RSAUtils.encrypt(ori, RSAUtils.buildPrivateKey(priKey));
//		System.out.println(Base64Utils.encode(cipher));
		byte[] back = RSAUtils.decrypt(cipher, RSAUtils.buildPublicKey(pubKey));
		assertTrue(Arrays.equals(ori, back));

		// 公钥加密
		cipher = RSAUtils.encrypt(ori, RSAUtils.buildPublicKey(pubKey));
//		System.out.println(Base64Utils.encode(cipher));
		back = RSAUtils.decrypt(cipher, RSAUtils.buildPrivateKey(priKey));
		assertTrue(Arrays.equals(ori, back));
	}
	
	@Test
	public void encrypt_decrypt() throws Exception {
		byte[] pubKey = Hex.decodeHex(pubKeyStr.toCharArray());
		byte[] priKey = Hex.decodeHex(priKeyStr.toCharArray());

		// PKCS1Padding 公钥加密
		byte[] cipher = RSAUtils.encrypt(ori, RSAUtils.buildPublicKey(pubKey),
				Transformation.RSA_ECB_PKCS1Padding);
//		System.out.println(Base64Utils.encode(cipher));
		byte[] back = RSAUtils.decrypt(cipher, RSAUtils.buildPrivateKey(priKey),
				Transformation.RSA_ECB_PKCS1Padding);
		assertTrue(Arrays.equals(ori, back));
		
		// PKCS1Padding 私钥加密
		cipher = RSAUtils.encrypt(ori, RSAUtils.buildPrivateKey(priKey),
				Transformation.RSA_ECB_PKCS1Padding);
//		System.out.println(Base64Utils.encode(cipher));
		back = RSAUtils.decrypt(cipher, RSAUtils.buildPublicKey(pubKey),	
				Transformation.RSA_ECB_PKCS1Padding);
		assertTrue(Arrays.equals(ori, back));
		
		
		
		// OAEP不能用于签名和验证（即不能用于私钥加密）！
		
		// OAEPWithSHA1 公钥加密
		cipher = RSAUtils.encrypt(ori, RSAUtils.buildPublicKey(pubKey),
				Transformation.RSA_ECB_OAEPWithSHA1AndMGF1Padding);
//		System.out.println(Base64Utils.encode(cipher));
		back = RSAUtils.decrypt(cipher, RSAUtils.buildPrivateKey(priKey),
				Transformation.RSA_ECB_OAEPWithSHA1AndMGF1Padding);
		assertTrue(Arrays.equals(ori, back));
		
		// OAEPWithSHA256 公钥加密
		cipher = RSAUtils.encrypt(ori, RSAUtils.buildPublicKey(pubKey),
				Transformation.RSA_ECB_OAEPWithSHA256AndMGF1Padding);
//		System.out.println(Base64Utils.encode(cipher));
		back = RSAUtils.decrypt(cipher, RSAUtils.buildPrivateKey(priKey),
				Transformation.RSA_ECB_OAEPWithSHA256AndMGF1Padding);
		assertTrue(Arrays.equals(ori, back));
	}
}
