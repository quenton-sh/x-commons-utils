package x.commons.util.http;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class HttpUtilsTest {

	@Test
	public void get() throws Exception {
		StreamResponse res = HttpUtils.get("http://iframe.ip138.com/ic.asp", null);
		assertEquals(200, res.getStatus());
		InputStream in = res.getInputStream();
		byte[] bodyData = IOUtils.toByteArray(in);
		res.close();
		
		Map<String, String> resHeaders = res.getHeaders();
		int len = Integer.parseInt(resHeaders.get("Content-Length"));
		assertEquals(len, bodyData.length);
	}
	
	@Test
	public void getAndClose() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/html;charset=UTF-8");
		headers.put("User-Agent", "test");
		
		DataResponse res = HttpUtils.getAndClose("http://iframe.ip138.com/ic.asp", headers);
		assertEquals(200, res.getStatus());
		byte[] bodyData = res.getBodyData();
		
		Map<String, String> resHeaders = res.getHeaders();
		int len = Integer.parseInt(resHeaders.get("Content-Length"));
		assertEquals(len, bodyData.length);
	}
	
}
