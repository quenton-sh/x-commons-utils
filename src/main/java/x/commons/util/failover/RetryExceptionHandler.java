package x.commons.util.failover;

public interface RetryExceptionHandler {

	/**
	 * @param e
	 * @param leftTryCount 剩余可重试次数
	 * @param dead 是否已没有重试机会（此参数值相当于计算 leftTryCount <= 0）
	 * @throws Exception
	 */
	public void handleException(Exception e, boolean dead, int leftTryCount) throws Exception;
}
