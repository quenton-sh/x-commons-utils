package x.commons.util;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RetrySupport {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private int failRetryCount = 2; // 失败重试次数
	private int failRetryIntervalMillis = 1000; // 失败多次重试之间的间隔时间（毫秒）
	
	public RetrySupport() {
		
	}
	
	public RetrySupport(int failRetryCount, int failRetryIntervalMillis) {
		this.failRetryCount = failRetryCount;
		this.failRetryIntervalMillis = failRetryIntervalMillis;
	}

	public int getFailRetryCount() {
		return failRetryCount;
	}

	public void setFailRetryCount(int failRetryCount) {
		this.failRetryCount = failRetryCount;
	}

	public int getFailRetryIntervalMillis() {
		return failRetryIntervalMillis;
	}

	public void setFailRetryIntervalMillis(int failRetryIntervalMillis) {
		this.failRetryIntervalMillis = failRetryIntervalMillis;
	}

	public <T> T callWithRetry(Callable<T> callable) throws Exception {
		int leftRunTimes = 1; // 剩余的循环体可运行次数
		if (this.failRetryCount > 0) {
			leftRunTimes += this.failRetryCount;
		}
		T retVal = null;
		while (true) {
			leftRunTimes --;
			try {
				retVal = callable.call();
				break;
			} catch (Exception e) {
				this.logException(e, leftRunTimes);
				if (leftRunTimes <= 0) {
					throw e; 
				} else if (this.failRetryIntervalMillis > 0) {
					try {
						Thread.sleep(failRetryIntervalMillis);
					} catch (InterruptedException e1) {
						
					}
				}
			}
		}
		return retVal;
	}
	
	protected String buildExceptionMsg(Exception e, int leftRetryTime) {
		String exceptionName = e.getClass().getName();
		if (leftRetryTime > 0) {
			if (this.failRetryIntervalMillis > 0) {
				return String.format("%s: '%s' caught, %d retry times left. Retry in %d milliseconds.",
								exceptionName, e.getMessage(), leftRetryTime,
								this.failRetryIntervalMillis);
			} else {
				return String.format("%s: '%s' caught, %d retry times left. Retry immediately.",
								exceptionName, e.getMessage(), leftRetryTime);
			}
		} else {
			return String.format("%s: '%s' caught, 0 retry times left. Failed!",
					exceptionName, e.getMessage());
		}
	}
	
	protected void logException(Exception e, int leftRetryTime) {
		String msg = this.buildExceptionMsg(e, leftRetryTime);
		logger.error(msg, e);
	}
}
