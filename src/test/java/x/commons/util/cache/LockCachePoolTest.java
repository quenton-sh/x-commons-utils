package x.commons.util.cache;

import static org.junit.Assert.*;

import org.junit.Test;

public class LockCachePoolTest {

	@Test
	public void getLock() {
		LockCachePool sug = new LockCachePool(1);
		
		Object lock1 = sug.getLock("key1");
		Object lock2 = sug.getLock("key2");
		assertTrue(lock1 != lock2);
		
		Object anotherLock1 = sug.getLock("key1");
		assertTrue(lock1 != anotherLock1);
		
		sug = new LockCachePool(5);
		lock1 = sug.getLock("key1");
		lock2 = sug.getLock("key2");
		assertTrue(lock1 != lock2);
		
		anotherLock1 = sug.getLock("key1");
		assertTrue(lock1 == anotherLock1);
	}
}
