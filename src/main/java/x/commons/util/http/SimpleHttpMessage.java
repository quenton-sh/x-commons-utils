package x.commons.util.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 * 非线程安全
 * @author Quenton
 *
 */
abstract class SimpleHttpMessage {
	
	public static enum HttpVersion {
		HTTP_1_0,
		HTTP_1_1
	}
	
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private HttpEntity entity = null;
	private HttpVersion httpVersion = null;
	
	public void addHeader(String name, String value) {
		name = name.toLowerCase();
		List<String> list = this.headers.get(name);
		if (list == null) {
			list = new ArrayList<String>(2);
			this.headers.put(name, list);
		}
		list.add(value);
	}
	
	public void setFirstHeader(String name, String value) {
		name = name.toLowerCase();
		List<String> list = this.headers.get(name);
		if (list == null) {
			list = new ArrayList<String>(2);
			this.headers.put(name, list);
		} 
		if (list.size() > 0) {
			list.set(0, value);
		} else {
			list.add(value);
		}
	}
	
	public void setHeaders(String name, List<String> value) {
		name = name.toLowerCase();
		this.headers.put(name, new ArrayList<String>(value));
	}
	
	public boolean containsHeader(String name) {
		name = name.toLowerCase();
		return this.headers.containsKey(name);
	}
	
	public boolean removeHeader(String name, String value) {
		name = name.toLowerCase();
		List<String> list = this.headers.get(name);
		if (list == null) {
			return false;
		}
		return list.remove(value);
	}
	
	public boolean removeHeaders(String name) {
		name = name.toLowerCase();
		return this.headers.remove(name) != null;
	}
	
	public Set<String> getHeaderNames() {
		return this.headers.keySet();
	}
	
	public String getFirstHeader(String name) {
		name = name.toLowerCase();
		List<String> values = this.headers.get(name);
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return values.get(0);
		}
	}
	
	public String getLastHeader(String name) {
		name = name.toLowerCase();
		List<String> values = this.headers.get(name);
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return values.get(values.size() - 1);
		}
	}
	
	public List<String> getHeaders(String name) {
		name = name.toLowerCase();
		List<String> values = this.headers.get(name);
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return values;
		}
	}
	
	public Map<String, List<String>> getAllHeaders() {
		return this.headers;
	}
	
	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setEntity(byte[] entity) {
		if (entity != null) {
			this.entity = new ByteArrayEntity(entity);
		} else {
			this.entity = null;
		}
	}
	
	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setEntity(String entity, String encoding) {
		if (entity != null) {
			this.entity = new StringEntity(entity, encoding);
		} else {
			this.entity = null;
		}
	}
	
	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setEntity(File file) {
		if (file != null) {
			this.entity = new FileEntity(file);
		} else {
			this.entity = null;
		}
	}
	
	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setEntity(InputStream in) {
		if (in != null) {
			this.entity = new InputStreamEntity(in);
		} else {
			this.entity = null;
		}
	}
	
	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setFormEntity(Map<String, String> formData, String encoding) throws UnsupportedEncodingException {
		if (formData != null) {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>(formData.size());
			for (Entry<String, String> entry : formData.entrySet()) {
				if (entry.getValue() != null) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
			}
			this.entity = new UrlEncodedFormEntity(nvps, encoding);
		} else {
			this.entity = null;
		}
	}

	/**
	 * 仅对POST,PUT有效
	 * @return
	 * @throws IOException
	 */
	public void setMultiPartFormEntity(Map<String, Object> formData, String encoding) {
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setCharset(Charset.forName(encoding));
		for (Entry<String, Object> entry : formData.entrySet()) {
			Object value = entry.getValue();
			if (value != null) {
				if (value instanceof byte[]) {
					builder.addBinaryBody(entry.getKey(), (byte[]) value);
				} else if (value instanceof String) {
					builder.addTextBody(entry.getKey(), (String) value);
				} else if (value instanceof File) {
					builder.addBinaryBody(entry.getKey(), (File) value);
				} else if (value instanceof InputStream) {
					builder.addBinaryBody(entry.getKey(), (InputStream) value);
				} else {
					throw new IllegalArgumentException(String.format("Unsupported type for multipart value: '%s'.", value.getClass().getName()));
				}
			}
		}
		this.entity = builder.build();
	}
	
	public byte[] getEntity() throws IOException {
		return this.entity == null ? null : IOUtils.toByteArray(this.entity.getContent());
	}
	
	public String getEntity(String encoding) throws IOException {
		return this.entity == null ? null : IOUtils.toString(this.entity.getContent(), encoding);
	}
	
	public InputStream getEntityAsStream() throws IOException {
		return this.entity == null ? null : this.entity.getContent();
	}
	
	protected HttpEntity getRawEntity() {
		return this.entity;
	}
	
	public HttpVersion getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(HttpVersion httpVersion) {
		this.httpVersion = httpVersion;
	}
	
	public void setGZIPCompressed(boolean enabled) {
		if (enabled) {
			this.setFirstHeader("Content-Encoding", "gzip");
		} else {
			this.removeHeaders("Content-Encoding");
		}
	}
	
	public boolean isGZIPCompressed() {
		return "gzip".equalsIgnoreCase(this.getFirstHeader("Content-Encoding"));
	}
}
