package x.commons.util.failover;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x.commons.util.failover.FailoverCallable;
import x.commons.util.failover.FailoverSupport;
import x.commons.util.failover.RetrySupport;

public class FailoverRetrySupport<RESOURCE_TYPE> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final FailoverSupport<RESOURCE_TYPE> failoverSupport;
	protected final RetrySupport retrySupport;

	public FailoverRetrySupport(List<RESOURCE_TYPE> resourceList, 
			int failRetryCount, int failRetryIntervalMillis) {
		this.failoverSupport = new FailoverSupport<RESOURCE_TYPE>(resourceList) {
			@Override
			protected void logException(Exception e, int index, boolean hasNext) {
				FailoverRetrySupport.this.logFailoverException(e, index, hasNext);
			}
		};
		this.retrySupport = new RetrySupport(failRetryCount, failRetryIntervalMillis) {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				FailoverRetrySupport.this.logRetryException(e, dead, leftRetryTime);
			}
		};
	}
	
	public FailoverRetrySupport(List<RESOURCE_TYPE> resourceList, 
			int failRetryCount, int failRetryIntervalMillis,
			RetryExceptionHandler retryExceptionHandler) {
		this.failoverSupport = new FailoverSupport<RESOURCE_TYPE>(resourceList) {
			@Override
			protected void logException(Exception e, int index, boolean hasNext) {
				FailoverRetrySupport.this.logFailoverException(e, index, hasNext);
			}
		};
		this.retrySupport = new RetrySupport(failRetryCount, failRetryIntervalMillis,
				retryExceptionHandler) {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				FailoverRetrySupport.this.logRetryException(e, dead, leftRetryTime);
			}
		};
	}
	
	public <RETURN_TYPE> RETURN_TYPE callWithFailoverRetry(
			final FailoverCallable<RETURN_TYPE, RESOURCE_TYPE> callable) throws Exception {
		return this.failoverSupport.callWithFailover(new FailoverCallable<RETURN_TYPE, RESOURCE_TYPE>() {
			@Override
			public RETURN_TYPE call(final RESOURCE_TYPE resource, final int resourceIndex) throws Exception {
				return retrySupport.callWithRetry(new Callable<RETURN_TYPE>() {
					@Override
					public RETURN_TYPE call() throws Exception {
						return callable.call(resource, resourceIndex);
					}
				});
			}
		});
	}

	protected void logFailoverException(Exception e, int index, boolean hasNext) {
		String msg = this.failoverSupport.buildExceptionMsg(e, index, hasNext);
		logger.error(msg, e);
	}
	
	protected void logRetryException(Exception e, boolean dead, int leftRetryTime) {
		String msg = this.retrySupport.buildExceptionMsg(e, dead, leftRetryTime);
		logger.error(msg, e);
	}
}
