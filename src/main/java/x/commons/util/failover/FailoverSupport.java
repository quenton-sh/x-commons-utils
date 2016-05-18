package x.commons.util.failover;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailoverSupport<RESOURCE_TYPE> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final List<RESOURCE_TYPE> resourceList;
	private int resourceIndex = 0;
	
	public FailoverSupport(List<RESOURCE_TYPE> resourceList) {
		if (resourceList == null) {
			throw new IllegalArgumentException("The constructor arg must not be null.");
		}
		if (resourceList.size() == 0) {
			throw new IllegalArgumentException("The size of the constructor arg must not be 0.");
		}
		this.resourceList = resourceList;
	}

	public <RETURN_TYPE> RETURN_TYPE callWithFailover(
			FailoverCallable<RETURN_TYPE, RESOURCE_TYPE> callable) throws Exception {
		int index = this.resourceIndex;
		int tryCount = 0;
		RETURN_TYPE retObj = null;
		while (true) {
			tryCount ++;
			try {
				RESOURCE_TYPE resource = this.resourceList.get(index);
				retObj = callable.call(resource, index);
				this.resourceIndex = index;
				break;
			} catch (Exception e) {
				if (tryCount >= this.resourceList.size()) {
					// 所有资源已经都试过一遍
					this.logException(e, index, false);
					throw e;
				} else {
					this.logException(e, index, true);
					index = this.getNextResourceIndex(index);
				}
			}
		}
		return retObj;
	}

	protected String buildExceptionMsg(Exception e, int index, boolean hasNext) {
		String exceptionName = e.getClass().getName();
		if (hasNext) {
			return String.format(
					"%s: '%s' caught for resource[%d], try next.", 
					exceptionName, e.getMessage(), index);
		} else {
			return String.format(
					"%s: '%s' caught for resource[%d], no alternative resource left. Failed!", 
					exceptionName, e.getMessage(), index);
		}
	}

	protected void logException(Exception e, int index, boolean hasNext) {
		String msg = this.buildExceptionMsg(e, index, hasNext);
		logger.error(msg, e);
	}
	
	protected int getNextResourceIndex(int currentIndex) {
		int nextIndex = currentIndex + 1;
		if (nextIndex >= this.resourceList.size()) {
			nextIndex = 0;
		}
		return nextIndex;
	}
}
