package x.commons.util.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import x.commons.util.http.SimpleHttpConnectionPoolConfig.HttpHost;
import x.commons.util.http.SimpleHttpRequest.HttpMethod;

public class SimpleHttpClientTestManual {
	
	private static SimpleHttpClient client = null;
	
	@BeforeClass
	public static void init() {
		SimpleHttpConfig config = new SimpleHttpConfig();
		SimpleHttpConnectionPoolConfig poolConfig = new SimpleHttpConnectionPoolConfig();
		poolConfig.setMaxForHost(new HttpHost("20140507.ip138.com"), 5);
		client = new SimpleHttpClient(config, poolConfig);
	}

	@Test
	public void testStream() throws IOException {
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.GET, "http://20140507.ip138.com/ic.asp");
		SimpleHttpResponse res = client.request(req);
		
		assertEquals(200, res.getStatus());
		InputStream in = res.getEntityAsStream();
		byte[] bodyData = IOUtils.toByteArray(in);
		
		int len = Integer.parseInt(res.getFirstHeader("Content-Length"));
		assertEquals(len, bodyData.length);
		
		res.close();
	}
	
	@Test
	public void testByteArray() throws IOException {
		SimpleHttpRequest req = new SimpleHttpRequest(HttpMethod.GET, "http://20140507.ip138.com/ic.asp");
		req.addHeader("Content-Type", "text/html;charset=UTF-8");
		req.addHeader("User-Agent", "test");
		SimpleHttpResponse res = client.request(req);
		
		assertEquals(200, res.getStatus());
		byte[] bodyData = res.getEntity();
		
		int len = Integer.parseInt(res.getFirstHeader("Content-Length"));
		assertEquals(len, bodyData.length);
		
		res.close();
	}
	
	@Test
	public void testConnectionPool() throws InterruptedException {
		// 测试连接池效果，打开 netstat -an | grep ESTABLISH 来查看
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					testByteArray();
					System.out.println("Thread-" + Thread.currentThread().getId() + " finished.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		ExecutorService exec = Executors.newFixedThreadPool(100);
		for (int i = 0; i < 100; i++) {
			exec.submit(run);
		}
		exec.shutdown();
		exec.awaitTermination(60, TimeUnit.SECONDS);
	}
	
	@AfterClass
	public static void cleanup() throws IOException {
		client.close();
		client = null;
	}
}
