package x.commons.util.cache;

import org.apache.commons.collections4.map.LRUMap;

public class LRUCache<T> {

	private LRUMap<String, CacheItem<T>> lruMap;
	private LockCachePool lockCachePool;

	public LRUCache(int size) {
		this.lruMap = new LRUMap<String, CacheItem<T>>(size);
		this.lockCachePool = new LockCachePool(size);
	}
	
	public void set(String key, int expireSecs, T value) {
		Object lock = this.lockCachePool.getLock(key);
		synchronized (lock) {
			Long expireTs = null;
			if (expireSecs > 0) {
				expireTs = System.currentTimeMillis() + expireSecs * 1000;
			}
			CacheItem<T> cache = new CacheItem<T>();
			cache.setExpireTs(expireTs);
			cache.setValue(value);
			this.lruMap.put(key, cache);
		}
	}
	
	public T get(String key) {
		Object lock = this.lockCachePool.getLock(key);
		synchronized (lock) {
			CacheItem<T> cache = this.lruMap.get(key);
			if (cache != null) {
				Long expireTs = cache.getExpireTs();
				if (expireTs == null) {
					// never expire
					return cache.getValue();
				}
				if (System.currentTimeMillis() <= expireTs) {
					// not expired
					return cache.getValue();
				} else {
					// expired
					this.lruMap.remove(key);
				}
			}
			return null;
		}
	}
	
	public T remove(String key) {
		Object lock = this.lockCachePool.getLock(key);
		synchronized (lock) {
			CacheItem<T> cache = this.lruMap.remove(key);
			return cache == null ? null : cache.getValue();
		}
	}
}
