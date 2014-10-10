package x.commons.util.http;

import java.util.HashMap;
import java.util.Map;

public class SimpleHttpConnectionPoolConfig {
	
	public static class HttpHost {
		private String schemeName = "http";
		private String hostName;
		private int port = 80;
		
		public HttpHost(String hostName) {
			this.hostName = hostName.toLowerCase();
		}

		public HttpHost(String schemeName, String hostName, int port) {
			this(hostName);
			this.schemeName = schemeName.toLowerCase();
			this.port = port;
		}

		public String getSchemeName() {
			return schemeName;
		}

		public String getHostName() {
			return hostName;
		}

		public int getPort() {
			return port;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof HttpHost)) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			return this.toString().equals(obj.toString());
		}
		
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public String toString() {
			return String.format("%s://%s:%d", this.schemeName, this.hostName, this.port);
		}
	}
	
	private boolean enabled = true;
	private int maxTotal = 200;
	private int defaultMaxPerHost = 20;
	private Map<HttpHost, Integer> hostMaxConfigs = new HashMap<HttpHost, Integer>();
	private int connectinRetrievingTimeout = 5000;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	public int getDefaultMaxPerHost() {
		return defaultMaxPerHost;
	}

	public void setDefaultMaxPerHost(int defaultMaxPerHost) {
		this.defaultMaxPerHost = defaultMaxPerHost;
	}

	public void setMaxForHost(HttpHost host, int max) {
		this.hostMaxConfigs.put(host, max);
	}
	
	public int getMaxForHost(HttpHost host) {
		Integer value = this.hostMaxConfigs.get(host);
		if (value != null) {
			return value;
		} else {
			return this.defaultMaxPerHost;
		}
	}
	
	/**
	 * 从连接池中获取连接的超时时间(毫秒)
	 * @return
	 */
	public int getConnectinRetrievingTimeout() {
		return connectinRetrievingTimeout;
	}

	/**
	 * 设置从连接池中获取连接的超时时间(毫秒)
	 * @param connectinRetrievingTimeout
	 */
	public void setConnectinRetrievingTimeout(int connectinRetrievingTimeout) {
		this.connectinRetrievingTimeout = connectinRetrievingTimeout;
	}

	protected Map<HttpHost, Integer> getHostMaxConfigs() {
		return this.hostMaxConfigs;
	}

}
