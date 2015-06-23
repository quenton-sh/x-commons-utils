package x.commons.util.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;


public class SimpleHttpResponse extends SimpleHttpMessage implements Closeable {

	private int status;
	private String reason;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public void close() throws IOException {
		HttpEntity entity = super.getRawEntity();
		if (entity != null) {
			InputStream in = entity.getContent();
			if (in != null) {
				in.close();
			}
		}
	}

}
