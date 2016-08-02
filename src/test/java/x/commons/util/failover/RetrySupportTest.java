package x.commons.util.failover;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class RetrySupportTest {
	
	@SuppressWarnings("serial")
	private static class MyException extends Exception {
		public MyException(String s) {
			super(s);
		}
	}
	
	private final AtomicInteger runCount = new AtomicInteger(0);
	private final Callable<Boolean> action = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			int rc = runCount.addAndGet(1);
			if (rc >= 3) {
				return true;
			} else {
				throw new MyException("Failed run, seq: " + rc);
			}
		}
	};
	
	@Before
	public void init() {
		runCount.set(0);
	}

	@Test
	public void test1() throws Exception {
		// 尝试2次，失败
		RetrySupport sug = new RetrySupport(1, 0) {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, dead, leftRetryTime);
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
	}
	
	@Test
	public void test2() throws Exception {
		// 尝试4次，通过
		RetrySupport sug = new RetrySupport() {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, dead, leftRetryTime);
				System.out.println(msg);
			}
		};
		sug.setFailRetryCount(3);
		sug.setFailRetryIntervalMillis(100);
		long l1 = System.currentTimeMillis();
		assertTrue(sug.callWithRetry(action));
		long l2 = System.currentTimeMillis();
		// 每次重试间隔100ms，第三次运行通过，则至少耗时200ms
		assertTrue(l2 - l1 > 200);
	}
	
	@Test
	public void test3() throws Exception {
		// 添加ExceptionHandler，不抛出异常
		RetryExceptionHandler exh = new RetryExceptionHandler() {
			@Override
			public void handleException(Exception e, boolean dead, int leftTryCount) throws Exception {
				System.out.println(String.format("ExceptionHandler: \"%s\", %s, %d", e.getMessage(), String.valueOf(dead), leftTryCount));
				assertTrue(e instanceof MyException);
				if (leftTryCount > 0) {
					assertTrue(!dead);
				} else {
					assertTrue(dead);
				}
			}
		};
		
		RetrySupport sug = new RetrySupport(1, 100, exh) {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, dead, leftRetryTime);
				System.out.println(msg);
			}
		};
		long l1 = System.currentTimeMillis();
		assertTrue(sug.callWithRetry(action) == null);
		long l2 = System.currentTimeMillis();
		// 每次重试间隔100ms，运行两次，则至少耗时100ms
		assertTrue(l2 - l1 >= 100);
	}
	
	@Test
	public void test4() throws Exception {
		// 添加ExceptionHandler，抛出异常
		RetryExceptionHandler exh = new RetryExceptionHandler() {
			@Override
			public void handleException(Exception e, boolean dead, int leftTryCount) throws Exception {
				System.out.println(String.format("ExceptionHandler: \"%s\", %s, %d", e.getMessage(), String.valueOf(dead), leftTryCount));
				assertTrue(e instanceof MyException);
				if (leftTryCount > 0) {
					assertTrue(!dead);
				} else {
					assertTrue(dead);
					throw e;
				}
			}
		};
		
		RetrySupport sug = new RetrySupport(1, 100, exh) {
			@Override
			protected void logException(Exception e, boolean dead, int leftRetryTime) {
				String msg = this.buildExceptionMsg(e, dead, leftRetryTime);
				System.out.println(msg);
			}
		};
		long l1 = System.currentTimeMillis();
		try {
			sug.callWithRetry(action);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof MyException);
			System.out.println(e.getMessage());
		}
		long l2 = System.currentTimeMillis();
		// 每次重试间隔100ms，运行两次，则至少耗时100ms
		assertTrue(l2 - l1 >= 100);
	}
}
