package x.commons.util.cache;

import static org.junit.Assert.*;

import org.junit.Test;

public class LRUCacheTest {
	
	@Test
	public void test() {
		String key = "key";
		String value = "value";
		LRUCache<String> sug = new LRUCache<String>(10);
		sug.set(key, 0, value);
		
		assertEquals(value, sug.get(key));
		
		String removed = sug.remove(key);
		assertEquals(value, removed);
		assertTrue(sug.get(key) == null);
		
		removed = sug.remove("anotherKey");
		assertTrue(removed == null);
	}
	
	@Test
	public void testLRU() {
		String value1 = new String("value1");
		LRUCache<String> sug = new LRUCache<String>(1);
		sug.set("key1", 500, value1);
		assertEquals("value1", sug.get("key1"));
		
		String value2 = new String("value2");
		sug.set("key2", 500, value2);
		assertEquals("value2", sug.get("key2"));
		
		assertTrue(sug.get("key1") == null);
		
		
		sug = new LRUCache<String>(5);
		sug.set("key1", 500, value1);
		assertEquals("value1", sug.get("key1"));
		
		value2 = new String("value2");
		sug.set("key2", 500, value2);
		assertEquals("value2", sug.get("key2"));
		
		assertEquals("value1", sug.get("key1"));
	}
	
	@Test
	public void testExpire() throws InterruptedException {
		LRUCache<String> sug = new LRUCache<String>(1);
		assertTrue(sug.get("key1") == null);
		
		sug.set("key1", 1, "value1");
		assertEquals("value1", sug.get("key1"));
		
		sug.set("key1", 1, "value2");
		assertEquals("value2", sug.get("key1"));
		
		Thread.sleep(1000);
		assertTrue(sug.get("key1") == null);
	}
}
