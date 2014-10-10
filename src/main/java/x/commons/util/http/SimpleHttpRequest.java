package x.commons.util.http;



public class SimpleHttpRequest extends SimpleHttpMessage {
	
	public static enum HttpMethod {
		GET,
		POST,
		PUT,
		DELETE,
		HEAD,
		OPTIONS
	}
	
	protected final String url;
	protected final HttpMethod httpMethod;
	
	public SimpleHttpRequest(HttpMethod httpMethod, String url) {
		this.httpMethod = httpMethod;
		this.url = url;
	}
	
	public HttpMethod getHttpMethod() {
		return this.httpMethod;
	}
	
	public String getURL() {
		return this.url;
	}

}
