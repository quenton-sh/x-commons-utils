package x.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {

	public static byte[] gzipCompress(byte[] raw) throws Exception {
		ByteArrayOutputStream dst = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(dst);
		gos.write(raw);
		gos.flush();
		gos.close();
		return dst.toByteArray();
	}
	
	
	public static byte[] gzipDecompress(byte[] data) throws Exception {
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[2048];
		int len = -1;
		while ((len = gis.read(buff)) != -1) {
			baos.write(buff, 0, len);
		}
		gis.close();
		baos.close();
		return baos.toByteArray();
	}
}
