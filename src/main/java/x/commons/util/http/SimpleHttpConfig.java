package x.commons.util.http;

public class SimpleHttpConfig {

	private int connectionTimeout = 15 * 1000;
	private int socketTimeout = 60 * 1000;
	private int socketBufferSize = 2048;

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
	}

}
