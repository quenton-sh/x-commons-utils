package x.commons.util.cache;

import org.apache.commons.collections4.map.LRUMap;

public class LockCachePool {

	private final LRUMap<String, byte[]> lruMap;
	
	public LockCachePool(int size) {
		this.lruMap = new LRUMap<String, byte[]>(size);
	}
	
	public synchronized Object getLock(String key) {
		byte[] lock = lruMap.get(key);
		if (lock == null) {
			lock = new byte[] {(byte)1};
			lruMap.put(key, lock);
		}
		return lock;
	}
}
