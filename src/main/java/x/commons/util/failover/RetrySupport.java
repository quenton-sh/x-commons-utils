package x.commons.util.failover;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RetrySupport {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private int failRetryCount = 2; // 失败重试次数
	private int failRetryIntervalMillis = 1000; // 失败多次重试之间的间隔时间（毫秒）
	private RetryExceptionHandler retryExceptionHandler;
	
	public RetrySupport() {
		
	}
	
	public RetrySupport(int failRetryCount, int failRetryIntervalMillis) {
		this.failRetryCount = failRetryCount;
		this.failRetryIntervalMillis = failRetryIntervalMillis;
	}
	
	public RetrySupport(int failRetryCount, int failRetryIntervalMillis, 
			RetryExceptionHandler retryExceptionHandler) {
		this.failRetryCount = failRetryCount;
		this.failRetryIntervalMillis = failRetryIntervalMillis;
		this.retryExceptionHandler = retryExceptionHandler;
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
	
	public RetryExceptionHandler getExceptionHandler() {
		return retryExceptionHandler;
	}

	public void setExceptionHandler(RetryExceptionHandler exceptionHandler) {
		this.retryExceptionHandler = exceptionHandler;
	}

	public <T> T callWithRetry(Callable<T> callable) throws Exception {
		int leftRunTimes = 1; // 剩余的循环体可运行次数
		if (this.failRetryCount > 0) {
			leftRunTimes += this.failRetryCount;
		}
		T retVal = null;
		while (true) {
			if (leftRunTimes <= 0) {
				break;
			}
			leftRunTimes --;
			try {
				retVal = callable.call();
				break;
			} catch (Exception e) {
				boolean dead = false;
				if (leftRunTimes <= 0) {
					dead = true;
				}
				if (!dead) {
					if (this.retryExceptionHandler != null) {
						this.retryExceptionHandler.handleException(e, dead, leftRunTimes);
					} else {
						this.logException(e, dead, leftRunTimes);
					}
					if (this.failRetryIntervalMillis > 0) {
						try {
							Thread.sleep(failRetryIntervalMillis);
						} catch (InterruptedException e1) {
							
						}
					}
				} else {
					if (this.retryExceptionHandler != null) {
						this.retryExceptionHandler.handleException(e, dead, leftRunTimes);
					} else {
						this.logException(e, dead, leftRunTimes);
						throw e;
					}
				}
			}
		}
		return retVal;
	}
	
	protected String buildExceptionMsg(Exception e, boolean dead, int leftRetryTime) {
		String exceptionName = e.getClass().getName();
		if (!dead) {
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
	
	protected void logException(Exception e, boolean dead, int leftRetryTime) {
		String msg = this.buildExceptionMsg(e, dead, leftRetryTime);
		logger.error(msg, e);
	}
}
