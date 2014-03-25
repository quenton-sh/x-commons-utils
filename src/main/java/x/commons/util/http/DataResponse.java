package x.commons.util.http;

import java.util.Map;

public class DataResponse extends ResponseCommons {

	private final byte[] bodyData;

	DataResponse(int status, String reason, Map<String, String> headers,
			byte[] bodyData) {
		super(status, reason, headers);
		this.bodyData = bodyData;
	}

	public byte[] getBodyData() {
		return bodyData;
	}

}
