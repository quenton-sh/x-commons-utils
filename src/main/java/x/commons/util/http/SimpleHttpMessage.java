package x.commons.util.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 * 非线程安全
 * 
 * @author Quenton
 * 
 */
abstract class SimpleHttpMessage {

	public static enum HttpVersion {
		HTTP_1_0, HTTP_1_1
	}

	private Map<String, HeaderValues> headers = new LinkedHashMap<String, HeaderValues>();
	private HttpEntity entity = null;
	private HttpVersion httpVersion = null;
	private CookieStore cookieStore = null;

	private static class HeaderValues {
		private String headerName;
		private List<String> values;

		private HeaderValues(String headerName, int capacity) {
			this.headerName = headerName;
			this.values = new ArrayList<String>(capacity);
		}

		public String getHeaderName() {
			return headerName;
		}

		public void setHeaderName(String headerName) {
			this.headerName = headerName;
		}

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public void addHeader(String name, String value) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null) {
			values = new HeaderValues(name, 2);
			this.headers.put(headerKey, values);
		}
		values.setHeaderName(name);
		values.getValues().add(value);
	}

	public void setFirstHeader(String name, String value) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null) {
			values = new HeaderValues(name, 2);
			this.headers.put(headerKey, values);
		}
		values.setHeaderName(name);
		if (values.getValues().size() > 0) {
			values.getValues().set(0, value);
		} else {
			values.getValues().add(value);
		}
	}

	public void setHeaders(String name, List<String> value) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null) {
			values = new HeaderValues(name, 0);
			this.headers.put(headerKey, values);
		}
		values.setHeaderName(name);
		values.setValues(value);
	}

	public boolean containsHeader(String name) {
		String headerKey = name.toLowerCase();
		return this.headers.containsKey(headerKey);
	}

	public boolean removeHeader(String name, String value) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null) {
			return false;
		}
		return values.getValues().remove(value);
	}

	public boolean removeHeaders(String name) {
		String headerKey = name.toLowerCase();
		return this.headers.remove(headerKey) != null;
	}

	public Set<String> getHeaderNames() {
		Set<String> set = new LinkedHashSet<String>(this.headers.size());
		for (HeaderValues values : this.headers.values()) {
			set.add(values.getHeaderName());
		}
		return set;
	}

	public String getFirstHeader(String name) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null || values.getValues().size() == 0) {
			return null;
		} else {
			return values.getValues().get(0);
		}
	}

	public String getLastHeader(String name) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null || values.getValues().size() == 0) {
			return null;
		} else {
			return values.getValues().get(values.getValues().size() - 1);
		}
	}

	public List<String> getHeaders(String name) {
		String headerKey = name.toLowerCase();
		HeaderValues values = this.headers.get(headerKey);
		if (values == null || values.getValues().size() == 0) {
			return null;
		} else {
			return values.getValues();
		}
	}

	public Map<String, List<String>> getAllHeaders() {
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>(this.headers.size());
		for (HeaderValues values : this.headers.values()) {
			map.put(values.getHeaderName(), new ArrayList<String>(values.getValues()));
		}
		return map;
	}

	/**
	 * 仅对POST,PUT有效
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @return
	 * @throws IOException
	 */
	public void setFormEntity(Map<String, String> formData, String encoding)
			throws UnsupportedEncodingException {
		if (formData != null) {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>(
					formData.size());
			for (Entry<String, String> entry : formData.entrySet()) {
				if (entry.getValue() != null) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry
							.getValue()));
				}
			}
			this.entity = new UrlEncodedFormEntity(nvps, encoding);
		} else {
			this.entity = null;
		}
	}

	/**
	 * 仅对POST,PUT有效
	 * 
	 * @return
	 * @throws IOException
	 */
	public void setMultiPartFormEntity(Map<String, Object> formData,
			String encoding) {
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
					throw new IllegalArgumentException(String.format(
							"Unsupported type for multipart value: '%s'.",
							value.getClass().getName()));
				}
			}
		}
		this.entity = builder.build();
	}

	public byte[] getEntity() throws IOException {
		return this.entity == null ? null : IOUtils.toByteArray(this.entity
				.getContent());
	}

	public String getEntity(String encoding) throws IOException {
		return this.entity == null ? null : IOUtils.toString(
				this.entity.getContent(), encoding);
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

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}
}
