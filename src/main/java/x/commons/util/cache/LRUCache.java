package x.commons.util.cache;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;

public class LRUCache<T> {

	private LRUMap<String, CacheItem<T>> lruMap;

	public LRUCache(int size) {
		this.lruMap = new LRUMap<String, CacheItem<T>>(size);
	}

	public synchronized void set(String key, int expireSecs, T value) {
		Long expireTs = null;
		if (expireSecs > 0) {
			expireTs = System.currentTimeMillis() + expireSecs * 1000;
		}
		CacheItem<T> cache = new CacheItem<T>();
		cache.setExpireTs(expireTs);
		cache.setValue(value);
		this.lruMap.put(key, cache);
	}

	public synchronized T get(String key) {
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

	public synchronized T remove(String key) {
		CacheItem<T> cache = this.lruMap.remove(key);
		return cache == null ? null : cache.getValue();
	}
	
	public synchronized Set<String> keySet() {
		Set<String> filteredKeySet = new HashSet<String>(this.lruMap.size());
		Set<String> expiredKeySet = new HashSet<String>();
		for (Entry<String, CacheItem<T>> entry : this.lruMap.entrySet()) {
			CacheItem<T> cache = entry.getValue();
			if (cache != null) {
				Long expireTs = cache.getExpireTs();
				if (expireTs == null) {
					// never expire
					filteredKeySet.add(entry.getKey());
				} else {
					if (System.currentTimeMillis() <= expireTs) {
						// not expired
						filteredKeySet.add(entry.getKey());
					} else {
						// expired
						expiredKeySet.add(entry.getKey());
					}
				}
			}
		}
		// remove expired items
		for (String key : expiredKeySet) {
			this.lruMap.remove(key);
		}
		
		return filteredKeySet;
	}
}
