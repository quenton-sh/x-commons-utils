package x.commons.util.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import x.commons.util.http.SimpleHttpConnectionPoolConfig.HttpHost;
import x.commons.util.http.SimpleHttpMessage.HttpVersion;

/**
 * 线程安全
 * @author Quenton
 *
 */
public class SimpleHttpClient implements Closeable {

	private final CloseableHttpClient client;
	
	public SimpleHttpClient() {
		this(new SimpleHttpConfig());
	}
	
	public SimpleHttpClient(SimpleHttpConfig config) {
		this(config, null);
	}

	public SimpleHttpClient(SimpleHttpConfig config, SimpleHttpConnectionPoolConfig connectionPoolConfig) {
		HttpClientBuilder clientBuilder = HttpClients.custom();
		
		// set request config
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setConnectTimeout(config.getConnectionTimeout());
		requestConfigBuilder.setSocketTimeout(config.getSocketTimeout());
		
		ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
		connectionConfigBuilder.setBufferSize(config.getSocketBufferSize());
		
		// set connection manager
		if (connectionPoolConfig != null && connectionPoolConfig.isEnabled()) {
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(connectionPoolConfig.getMaxTotal());
			cm.setDefaultMaxPerRoute(connectionPoolConfig.getDefaultMaxPerHost());
			
			Map<HttpHost, Integer> hostMaxConfigs = connectionPoolConfig.getHostMaxConfigs();
			for (Entry<HttpHost, Integer> entry : hostMaxConfigs.entrySet()) {
				org.apache.http.HttpHost host = new org.apache.http.HttpHost(
						entry.getKey().getHostName(),
						entry.getKey().getPort(),
						entry.getKey().getSchemeName());
				cm.setMaxPerRoute(new HttpRoute(host), entry.getValue());
			}
			
			clientBuilder.setConnectionManager(cm);
			
			requestConfigBuilder.setConnectionRequestTimeout(connectionPoolConfig.getConnectinRetrievingTimeout());
		}
		
		clientBuilder.setDefaultConnectionConfig(connectionConfigBuilder.build())
			.setDefaultRequestConfig(requestConfigBuilder.build());
		
		this.client = clientBuilder.build();
	}
	
	public SimpleHttpResponse request(SimpleHttpRequest request) throws IOException {
		return this.request(request, null);
	}
	
	public SimpleHttpResponse request(SimpleHttpRequest request, SimpleHttpConfig config) throws IOException {
		// set url & method
		HttpRequestBase requestBase = null;
		switch (request.getHttpMethod()) {
		case GET:
			requestBase = new HttpGet(request.getURL());
			break;
		case POST:
			requestBase = new HttpPost(request.getURL());
			break;
		case PUT:
			requestBase = new HttpPut(request.getURL());
			break;
		case DELETE:
			requestBase = new HttpDelete(request.getURL());
			break;
		case HEAD:
			requestBase = new HttpHead(request.getURL());
			break;
		case OPTIONS:
			requestBase = new HttpOptions(request.getURL());
			break;
		}
		
		// set version
		if (request.getHttpVersion() == HttpVersion.HTTP_1_0) {
			requestBase.setProtocolVersion(new ProtocolVersion("HTTP", 1, 0));
		} else {
			// "HTTP/1.1" is the default value 
			requestBase.setProtocolVersion(new ProtocolVersion("HTTP", 1, 1));
		}
		
		// add headers
		Map<String, List<String>> headers = request.getAllHeaders();
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String name = entry.getKey();
			for (String value : entry.getValue()) {
				requestBase.addHeader(name, value);
			}
		}
		
		// set entity
		if (requestBase instanceof HttpEntityEnclosingRequestBase && request.getRawEntity() != null) {
			((HttpEntityEnclosingRequestBase) requestBase).setEntity(request.getRawEntity());
		}
		
		HttpContext httpContext = null;
		// set context
		if (request.getCookieStore() != null) {
			httpContext = HttpClientContext.create();
			httpContext.setAttribute(HttpClientContext.COOKIE_STORE, request.getCookieStore());
		}
		
		// set config
		if (config != null) {
			RequestConfig rc = RequestConfig.copy(requestBase.getConfig())
					.setConnectTimeout(config.getConnectionTimeout())
					.setSocketTimeout(config.getSocketTimeout()).build();
			requestBase.setConfig(rc);
		}

		// do request
		CloseableHttpResponse response = null;
		if (httpContext != null) {
			response = this.client.execute(requestBase, httpContext);
		} else {
			response = this.client.execute(requestBase);
		}
		
		// parse response:
		SimpleHttpResponse retVal = new SimpleHttpResponse();
		
		// parse status line
		retVal.setReason(response.getStatusLine().getReasonPhrase());
		retVal.setStatus(response.getStatusLine().getStatusCode());
		int vMajor = response.getStatusLine().getProtocolVersion().getMajor();
		int vMinor = response.getStatusLine().getProtocolVersion().getMinor();
		if (vMajor == 1) {
			if (vMinor == 0) {
				retVal.setHttpVersion(HttpVersion.HTTP_1_0);
			} else if (vMinor == 1) {
				retVal.setHttpVersion(HttpVersion.HTTP_1_1);
			}
		}
		
		// parse headers
		Header[] resHeaders = response.getAllHeaders();
		if (resHeaders != null) {
			for (Header header : resHeaders) {
				retVal.addHeader(header.getName(), header.getValue());
			}
		}
		
		// parse entity
		if (response.getEntity() != null) {
			retVal.setEntity(response.getEntity().getContent());
		}
		
		// parse context
		retVal.setCookieStore(request.getCookieStore());
		
		return retVal;
	}
	
	@Override
	public void close() throws IOException {
		this.client.close();
	}
	
}
