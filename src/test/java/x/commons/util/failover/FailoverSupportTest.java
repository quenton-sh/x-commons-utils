package x.commons.util.failover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FailoverSupportTest {

	@Test
	public void test() throws Exception {
		// 0、2资源无效，第一次从0开始
		List<String> list = Arrays.asList("ip0", "ip1", "ip2");
		FailoverCallable<Integer, String> callable = new FailoverCallable<Integer, String>() {
			@Override
			public Integer call(String resource, int resourceIndex) throws Exception {
				assertEquals("ip" + resourceIndex, resource);
				if (resourceIndex == 0 || resourceIndex == 2) {
					throw new Exception("index=" + resourceIndex);
				} else {
					return resourceIndex;
				}
			}
		};
		FailoverSupport<String> sug = new FailoverSupport<String>(list) {
			@Override
			protected void logException(Exception e, int index, boolean hasNext) {
				String msg = this.buildExceptionMsg(e, index, hasNext);
				System.out.println(msg);
			}
		};
		int ret = sug.callWithFailover(callable);
		assertTrue(ret == 1);
		System.out.println("--------------------");
		
		// 0、2资源无效，第二次直接从1开始，一次性成功
		list = Arrays.asList("ip0", "ip1", "ip2");
		ret = sug.callWithFailover(callable);
		assertTrue(ret == 1);
		System.out.println("--------------------");
		
		// 资源全无效
		list = Arrays.asList("ip0", "ip1", "ip2");
		callable = new FailoverCallable<Integer, String>() {
			@Override
			public Integer call(String resource, int resourceIndex) throws Exception {
				throw new Exception("index=" + resourceIndex);
			}
		};
		try {
			ret = sug.callWithFailover(callable);
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("--------------------");
		
		// 只有一个资源，有效
		list = Arrays.asList("ip0");
		sug = new FailoverSupport<String>(list) {
			@Override
			protected void logException(Exception e, int index, boolean hasNext) {
				String msg = this.buildExceptionMsg(e, index, hasNext);
				System.out.println(msg);
			}
		};
		callable = new FailoverCallable<Integer, String>() {
			@Override
			public Integer call(String resource, int resourceIndex) throws Exception {
				return resourceIndex;
			}
		};
		ret = sug.callWithFailover(callable);
		assertTrue(ret == 0);
		ret = sug.callWithFailover(callable);
		assertTrue(ret == 0);
		System.out.println("--------------------");
		
		// 只有一个资源，无效
		list = Arrays.asList("ip0");
		sug = new FailoverSupport<String>(list) {
			@Override
			protected void logException(Exception e, int index, boolean hasNext) {
				String msg = this.buildExceptionMsg(e, index, hasNext);
				System.out.println(msg);
			}
		};
		callable = new FailoverCallable<Integer, String>() {
			@Override
			public Integer call(String resource, int resourceIndex) throws Exception {
				throw new Exception("index=" + resourceIndex);
			}
		};
		try {
			ret = sug.callWithFailover(callable);
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			ret = sug.callWithFailover(callable);
			fail();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
