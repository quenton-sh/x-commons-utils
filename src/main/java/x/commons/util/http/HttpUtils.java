package x.commons.util.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	
	private volatile static ClientConnectionManager connectionManager;
	
	static {
		initConnectionPool(new HttpConnectionPoolConfig());
	}
	
	public synchronized static void initConnectionPool(HttpConnectionPoolConfig poolConfig) {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
		         new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(
		         new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		cm.setMaxTotal(poolConfig.getMaxTotal());
		cm.setDefaultMaxPerRoute(poolConfig.getDefaultMaxPerRoute());
		
		ClientConnectionManager oldConnectionManager = HttpUtils.connectionManager;
		HttpUtils.connectionManager = cm;
		
		if (oldConnectionManager != null) {
			oldConnectionManager.shutdown();
		}
	}
	
	private static HttpClient buildHttpClient(HttpConfig config) {
		HttpClient client = new DefaultHttpClient(connectionManager);
		client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());
		client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
		return client;
	}
	
	private static interface ResponseBuilder {
		Object buildResponse(HttpRequestBase requestBase, int statusCode, String reason, 
				Map<String, String> resHeaders, HttpEntity entity) throws Exception;
	}
	
	private static class StreamResponseBuilder implements ResponseBuilder {
		@Override
		public Object buildResponse(HttpRequestBase httpRequestBase, int statusCode, String reason,
				Map<String, String> resHeaders, HttpEntity entity)
				throws Exception {
			InputStream in = null;
			if (entity != null) {
				in = entity.getContent();
			}
			StreamResponse res = new StreamResponse(statusCode, reason, resHeaders, in, httpRequestBase);
			return res;
		}
	}
	
	private static class DataResponseBuilder implements ResponseBuilder {
		@Override
		public Object buildResponse(HttpRequestBase httpRequestBase, int statusCode,
				String reason, Map<String, String> resHeaders, HttpEntity entity)
				throws Exception {
			byte[] bodyData = null;
			if (entity != null) {
				InputStream in = entity.getContent();
				bodyData = IOUtils.toByteArray(in);
			}
			
			EntityUtils.consumeQuietly(entity);
			httpRequestBase.releaseConnection();
			
			DataResponse res = new DataResponse(statusCode, reason, resHeaders, bodyData);
			return res;
		}
	}
	
	private static Object doPost(HttpPost post, Map<String, String> headers, Map<String, String> formData, String encoding, 
			byte[] bodyData, InputStream streamedData, HttpConfig config, ResponseBuilder resBuilder) throws Exception {
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				post.addHeader(entry.getKey(), entry.getValue());
			}
		}
		if (formData != null && encoding != null) {
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			for (Entry<String, String> entry : formData.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			post.setEntity(new UrlEncodedFormEntity(nvps, encoding));
		} else if (bodyData != null) {
			post.setEntity(new ByteArrayEntity(bodyData));
		} else if (streamedData != null) {
			post.setEntity(new InputStreamEntity(streamedData, -1));
		}
		HttpClient httpClient = buildHttpClient(config);
		HttpResponse response = httpClient.execute(post);
		
		// status code line:
		int statusCode = response.getStatusLine().getStatusCode();
		String reason = response.getStatusLine().getReasonPhrase();

		// response headers:
		Header[] resHeaders = response.getAllHeaders();
		Map<String, String> map = new HashMap<String, String>();
		for (Header header : resHeaders) {
			map.put(header.getName(), header.getValue());
		}

		// response content:
		HttpEntity entity = response.getEntity();
		return resBuilder.buildResponse(post, statusCode, reason, map, entity);
	}
	
	private static Object doGet(HttpGet get, Map<String, String> headers, 
			HttpConfig config, ResponseBuilder resBuilder) throws Exception {
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				get.addHeader(entry.getKey(), entry.getValue());
			}
		}
		HttpClient httpClient = buildHttpClient(config);
		HttpResponse response = httpClient.execute(get);

		// status code line:
		int statusCode = response.getStatusLine().getStatusCode();
		String reason = response.getStatusLine().getReasonPhrase();

		// response headers:
		Header[] resHeaders = response.getAllHeaders();
		Map<String, String> map = new HashMap<String, String>();
		for (Header header : resHeaders) {
			map.put(header.getName(), header.getValue());
		}

		// response content:
		HttpEntity entity = response.getEntity();
		return resBuilder.buildResponse(get, statusCode, reason, map, entity);
	}
	
	///////////////////////

	public static DataResponse getAndClose(String url,
			Map<String, String> headers) throws Exception {
		return getAndClose(url, headers, new HttpConfig());
	}
	
	public static DataResponse getAndClose(String url,
			Map<String, String> headers, HttpConfig config) throws Exception {
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(url);
			return (DataResponse) doGet(httpGet, headers, config, 
					new DataResponseBuilder());
		} catch (Exception e) {
			if (httpGet != null) {
				httpGet.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static StreamResponse get(String url, Map<String, String> headers) throws Exception {
		return get(url, headers, new HttpConfig());
	}
	
	public static StreamResponse get(String url, Map<String, String> headers, HttpConfig config) throws Exception {
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(url);
			return (StreamResponse) doGet(httpGet, headers, config, 
					new StreamResponseBuilder());
		} catch (Exception e) {
			if (httpGet != null) {
				httpGet.releaseConnection();
			}
			throw e;
		}
	}

	///////////////////////
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, byte[] body)
			throws Exception {
		return postAndClose(url, headers, body, new HttpConfig());
	}
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, byte[] body, HttpConfig config)
			throws Exception {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (DataResponse) doPost(httpPost, headers, null, null, body, null, config, 
					new DataResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, Map<String, String> formData, String encoding)
			throws Exception {
		return postAndClose(url, headers, formData, encoding, new HttpConfig());
	}
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, Map<String, String> formData, String encoding, HttpConfig config)
			throws Exception {
		if (encoding == null) {
			encoding = "UTF-8";
		}
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (DataResponse) doPost(httpPost, headers, formData, encoding, null, null, config, 
					new DataResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, InputStream streamedData)
			throws Exception {
		return postAndClose(url, headers, streamedData, new HttpConfig());
	}
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, InputStream streamedData, HttpConfig config)
			throws Exception {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (DataResponse) doPost(httpPost, headers, null, null, null, streamedData, config, 
					new DataResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static StreamResponse post(String url, Map<String, String> headers, byte[] body)
			throws Exception {
		return post(url, headers, body, new HttpConfig());
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, byte[] body, HttpConfig config)
			throws Exception {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (StreamResponse) doPost(httpPost, headers, null, null, body, null, config, 
					new StreamResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static StreamResponse post(String url, Map<String, String> headers, Map<String, String> formData, String encoding)
			throws Exception {
		return post(url, headers, formData, encoding, new HttpConfig());
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, Map<String, String> formData, String encoding, HttpConfig config)
			throws Exception {
		if (encoding == null) {
			encoding = "UTF-8";
		}
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (StreamResponse) doPost(httpPost, headers, formData, encoding, null, null, config, 
					new StreamResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
	
	///////////////////////
	
	public static StreamResponse post(String url, Map<String, String> headers, InputStream streamedData)
			throws Exception {
		return post(url, headers, streamedData, new HttpConfig());
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, InputStream streamedData, HttpConfig config)
			throws Exception {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			return (StreamResponse) doPost(httpPost, headers, null, null, null, streamedData, config, 
					new StreamResponseBuilder());
		} catch (Exception e) {
			if (httpPost != null) {
				httpPost.releaseConnection();
			}
			throw e;
		}
	}
}
