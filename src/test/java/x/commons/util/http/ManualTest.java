package x.commons.util.http;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import x.commons.util.http.SimpleHttpRequest.HttpMethod;

public class ManualTest {
	
	private static final String ENC = "UTF-8";

	public static void main(String[] args) throws IOException {
		String urlBase = "http://localhost:8080/th";
		SimpleHttpClient client = new SimpleHttpClient();
		
		testPostBinary(client, urlBase);
		testPostString(client, urlBase);
		
		CookieStore cookieStore = new BasicCookieStore();
		testPostFormData(client, urlBase, cookieStore);
		testPostFormData(client, urlBase, cookieStore);
		
		testPostMultiPart(client, urlBase);
		
		client.close();
	}
	
	private static void testPostBinary(SimpleHttpClient client, String urlBase) throws IOException {
		String url = urlBase + "/postbytes";
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.POST, url);
		req.setEntity(ManualTest.class.getResourceAsStream("/test-file1.txt"));
		SimpleHttpResponse res = client.request(req);
		System.out.println(res.getEntity(ENC));
		
		System.out.println();
	}
	
	private static void testPostString(SimpleHttpClient client, String urlBase) throws IOException {
		String url = urlBase + "/postbytes";
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.POST, url);
		req.setEntity("string entity test.", ENC);
		SimpleHttpResponse res = client.request(req);
		System.out.println(res.getEntity(ENC));
		
		System.out.println();
	}
	
	private static void testPostFormData(SimpleHttpClient client, String urlBase, CookieStore cookieStore) throws IOException {
		String url = urlBase + "/postform";
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.POST, url);
		req.addHeader("header1", "header_value11");
		req.addHeader("header1", "header_value12");
		req.addHeader("header2", "header_value2");
		
		BasicClientCookie reqCookie = new BasicClientCookie("REQ_" + UUID.randomUUID().toString(), "" + System.currentTimeMillis());
		reqCookie.setDomain("localhost");
		reqCookie.setPath("/th");
		reqCookie.setExpiryDate(new Date(Long.MAX_VALUE));
		cookieStore.addCookie(reqCookie);
		req.setCookieStore(cookieStore);
		
		Map<String, String> formData = new HashMap<String, String>();
		formData.put("key1", "value1");
		formData.put("key2", "value2");
		formData.put("key3", "value3");
		req.setFormEntity(formData, ENC);
		
		SimpleHttpResponse res = client.request(req);
		System.out.println(res.getEntity(ENC));
		
		System.out.println("cookies:");
		List<Cookie> cookies = cookieStore.getCookies();
		for (Cookie cookie : cookies) {
			System.out.println(String.format("name='%s', value='%s', path='%s', domain='%s', version='%d'", 
					cookie.getName(),
					cookie.getValue(),
					cookie.getPath(),
					cookie.getDomain(),
					cookie.getVersion()));
		}
		
		System.out.println();
	}
	
	private static void testPostMultiPart(SimpleHttpClient client, String urlBase) throws IOException {
		String url = urlBase + "/postmultipart";
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.POST, url);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("key1", "vallue1");
		data.put("key2", "vallue2");
		data.put("file1", new File(ManualTest.class.getResource("/test-file1.txt").getPath()));
		data.put("file2", new File(ManualTest.class.getResource("/test-file2.txt").getPath()));
		req.setMultiPartFormEntity(data, ENC);
		
		SimpleHttpResponse res = client.request(req);
		System.out.println(res.getEntity(ENC));
	}
}

