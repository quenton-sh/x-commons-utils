package x.commons.util.http;

import java.io.Closeable;
import java.io.IOException;


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
		super.getRawEntity().getContent().close();
	}

}
