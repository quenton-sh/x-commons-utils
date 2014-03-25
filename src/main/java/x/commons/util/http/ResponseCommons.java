package x.commons.util.http;

import java.util.Map;

abstract class ResponseCommons {

	protected final int status;
	protected final String reason;
	protected final Map<String, String> headers;
	

	ResponseCommons(int status, String reason, Map<String, String> headers) {
		this.status = status;
		this.reason = reason;
		this.headers = headers;
	}

	public int getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

}
