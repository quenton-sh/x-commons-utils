package x.commons.util.http;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;

public class StreamResponse extends ResponseCommons implements Closeable {

	protected final InputStream inputStream;
	protected final HttpRequestBase httpRequestBase;

	StreamResponse(int status, String reason, Map<String, String> headers,
			InputStream inputStream, HttpRequestBase httpRequestBase) {
		super(status, reason, headers);
		this.inputStream = inputStream;
		this.httpRequestBase = httpRequestBase;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
	
	@Override
	public void close() {
		IOUtils.closeQuietly(inputStream);
		if (this.httpRequestBase != null) {
			this.httpRequestBase.releaseConnection();
		}
	}
}
