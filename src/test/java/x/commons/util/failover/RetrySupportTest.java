package x.commons.util.failover;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class RetrySupportTest {

	@Test
	public void test() throws Exception {
		final AtomicInteger runCount = new AtomicInteger(0);
		Callable<Boolean> action = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				int rc = runCount.addAndGet(1);
				if (rc >= 3) {
					return true;
				} else {
					throw new Exception("Failed run, seq: " + rc);
				}
			}
		};
		
		// 尝试2次，失败
		RetrySupport sug = new RetrySupport(1, 0) {
			@Override
			protected void logException(Exception e, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, leftRetryTime);
				System.out.println(msg);
			}
		};
		long l1 = System.currentTimeMillis();
		try {
			sug.callWithRetry(action);
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		long l2 = System.currentTimeMillis();
		// 每次重试间隔0ms，运行两次，耗时不可能超过50ms
		assertTrue(l2 - l1 < 50);
		
		runCount.set(0);
		// 尝试4次，通过
		sug = new RetrySupport() {
			@Override
			protected void logException(Exception e, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, leftRetryTime);
				System.out.println(msg);
			}
		};
		sug.setFailRetryCount(3);
		sug.setFailRetryIntervalMillis(100);
		l1 = System.currentTimeMillis();
		assertTrue(sug.callWithRetry(action));
		l2 = System.currentTimeMillis();
		// 每次重试间隔100ms，第三次运行通过，则至少耗时200ms
		assertTrue(l2 - l1 > 200);
	}
}
