package x.commons.util.http;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.conn.ClientConnectionManager;

public class StreamResponse extends ResponseCommons {

	protected final InputStream inputStream;
	protected final ClientConnectionManager connManager;

	StreamResponse(int status, String reason, Map<String, String> headers,
			InputStream inputStream, ClientConnectionManager connManager) {
		super(status, reason, headers);
		this.inputStream = inputStream;
		this.connManager = connManager;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void close() {
		this.connManager.shutdown();
	}
}
