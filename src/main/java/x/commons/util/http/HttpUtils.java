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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtils {
	
	private static interface ResponseBuilder {
		Object buildResponse(HttpClient client, int statusCode, String reason, 
				Map<String, String> resHeaders, HttpEntity entity) throws Exception;
	}
	
	private static class StreamResponseBuilderImpl implements ResponseBuilder {
		@Override
		public Object buildResponse(HttpClient client, int statusCode, String reason,
				Map<String, String> resHeaders, HttpEntity entity)
				throws Exception {
			InputStream in = null;
			if (entity != null) {
				in = entity.getContent();
			}
			StreamResponse res = new StreamResponse(statusCode, reason, resHeaders, in, client.getConnectionManager());
			return res;
		}
	}
	
	private static class DataResponseBuilderImpl implements ResponseBuilder {
		@Override
		public Object buildResponse(HttpClient client, int statusCode,
				String reason, Map<String, String> resHeaders, HttpEntity entity)
				throws Exception {
			byte[] bodyData = null;
			if (entity != null) {
				InputStream in = entity.getContent();
				bodyData = IOUtils.toByteArray(in);
			}
			DataResponse res = new DataResponse(statusCode, reason, resHeaders,
					bodyData);
			return res;
		}
	}
	
	private static Object doPost(HttpClient client, String url, 
			Map<String, String> headers, Map<String, String> formData, String encoding, 
			byte[] bodyData, InputStream streamedData, ResponseBuilder resBuilder) throws Exception {
		HttpPost post = new HttpPost(url);
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

		// status code line:
		HttpResponse response = client.execute(post);
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
		return resBuilder.buildResponse(client, statusCode, reason, map, entity);
	}
	
	private static Object doGet(HttpClient client, String url, 
			Map<String, String> headers, ResponseBuilder resBuilder) throws Exception {
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				get.addHeader(entry.getKey(), entry.getValue());
			}
		}

		// status code line:
		HttpResponse response = client.execute(get);
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
		return resBuilder.buildResponse(client, statusCode, reason, map, entity);
	}
	
	

	public static DataResponse getAndClose(String url,
			Map<String, String> headers) throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (DataResponse) doGet(client, url, headers, 
					new DataResponseBuilderImpl());
		} finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
	
	public static StreamResponse get(String url, Map<String, String> headers) throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (StreamResponse) doGet(client, url, headers, 
					new StreamResponseBuilderImpl());
		} catch (Exception e) {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
			throw e;
		}
	}

	public static DataResponse postAndClose(String url, Map<String, String> headers, byte[] body)
			throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (DataResponse) doPost(client, url, headers, null, null, 
					body, null, new DataResponseBuilderImpl());
		} finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, Map<String, String> formData, String encoding)
			throws Exception {
		HttpClient client = null;
		if (encoding == null) {
			encoding = "UTF-8";
		}
		try {
			client = new DefaultHttpClient();
			return (DataResponse) doPost(client, url, headers, formData, encoding, 
					null, null, new DataResponseBuilderImpl());
		} finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
	
	public static DataResponse postAndClose(String url, Map<String, String> headers, InputStream streamedData)
			throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (DataResponse) doPost(client, url, headers, null, null, 
					null, streamedData, new DataResponseBuilderImpl());
		} finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, byte[] body)
			throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (StreamResponse) doPost(client, url, headers, null, null, 
					body, null, new StreamResponseBuilderImpl());
		} catch (Exception e) {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
			throw e;
		}
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, Map<String, String> formData, String encoding)
			throws Exception {
		HttpClient client = null;
		if (encoding == null) {
			encoding = "UTF-8";
		}
		try {
			client = new DefaultHttpClient();
			return (StreamResponse) doPost(client, url, headers, formData, encoding, 
					null, null, new StreamResponseBuilderImpl());
		} catch (Exception e) {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
			throw e;
		}
	}
	
	public static StreamResponse post(String url, Map<String, String> headers, InputStream streamedData)
			throws Exception {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			return (StreamResponse) doPost(client, url, headers, null, null, 
					null, streamedData, new StreamResponseBuilderImpl());
		} catch (Exception e) {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
			throw e;
		}
	}
}
