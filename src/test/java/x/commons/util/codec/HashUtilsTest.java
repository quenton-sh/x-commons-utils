package x.commons.util.codec;

import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import x.commons.util.HashUtils;


public class HashUtilsTest {

	private static final String oriStr = "commons-utils-hashutil-test";
	private static final byte[] ori = oriStr.getBytes();

	@Test
	public void crc32() throws Exception {
		String s = HashUtils.crc32(ori);
		// System.out.println(s);
		assertEquals("f9c967e5", s);
	}

	@Test
	public void md5() throws Exception {
		byte[] md5Data = HashUtils.md5(ori);
		String s = Hex.encodeHexString(md5Data);
		// System.out.println(s);
		assertEquals("9c1338decb26c06cd7422135c279bdd9", s);
	}

	@Test
	public void sha1() throws Exception {
		byte[] sha1Data = HashUtils.sha1(ori);
		String s = Hex.encodeHexString(sha1Data);
		// System.out.println(s);
		assertEquals("ac5cc1d3276737bed2ed3bd073742cfb558189a3", s);
	}

}
