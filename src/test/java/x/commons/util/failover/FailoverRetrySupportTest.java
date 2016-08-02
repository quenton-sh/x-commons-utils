package x.commons.util.failover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class FailoverRetrySupportTest {

	@Test
	public void test() throws Exception {
		// 0、1资源无效，2有效
		// 第1、2次执行无效，第三次执行有效
		List<String> resourceList = Arrays.asList("ip0", "ip1", "ip2");
		final AtomicInteger tryCount = new AtomicInteger(0);
		final ThreadLocal<String> resourceRef = new ThreadLocal<String>();
		FailoverCallable<Integer, String> callable = new FailoverCallable<Integer, String>() {
			@Override
			public Integer call(String resource, int index)
					throws Exception {
				if (resourceRef.get() != resource) {
					// 说过刚刚failover切换到新资源上
					tryCount.set(0);
					resourceRef.set(resource);
					System.out.println("now try resource=" + resource);
				}
				if (tryCount.incrementAndGet() <= 3) {
					System.out.println("try time: " + tryCount.get() + " going to throw an exception.");
					throw new Exception("try=" + tryCount.get());
				}
				System.out.println("try time: " + tryCount.get() + " succeeded.");
				assertEquals("ip" + index, resource);
				if (index == 0 || index == 1) {
					System.out.println("index: " + index + " going to throw an exception.");
					throw new Exception("index=" + index);
				}
				return index;
			}
		};
		
		// 重试两次，失败
		tryCount.set(0);
		FailoverRetrySupport<String> sug = new FailoverRetrySupport<String>(resourceList, 2, 0) {
			@Override
			protected void logFailoverException(Exception e, int index, boolean hasNext) {
				String msg = this.failoverSupport.buildExceptionMsg(e, index, hasNext);
				System.out.println(msg);
			}
			
			@Override
			protected void logRetryException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.retrySupport.buildExceptionMsg(e, dead, leftRetryTime);
				System.out.println(msg);
			}
		};
		try {
			sug.callWithFailoverRetry(callable);
			fail();
		} catch (Exception e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		}

		System.out.println("--------------------");
		
		// 重试4次，成功
		sug = new FailoverRetrySupport<String>(resourceList, 4, 0) {
			@Override
			protected void logFailoverException(Exception e, int index, boolean hasNext) {
				String msg = this.failoverSupport.buildExceptionMsg(e, index, hasNext);
				System.out.println(msg);
			}
			
			@Override
			protected void logRetryException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.retrySupport.buildExceptionMsg(e, dead, leftRetryTime);
				System.out.println(msg);
			}
		};
		int ret = sug.callWithFailoverRetry(callable);
		assertTrue(ret == 2);
	}
}
