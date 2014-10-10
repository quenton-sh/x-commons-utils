package x.commons.util.cache;

class CacheItem<T> {

	private Long expireTs;
	private T value;

	Long getExpireTs() {
		return expireTs;
	}

	void setExpireTs(Long expireTs) {
		this.expireTs = expireTs;
	}

	T getValue() {
		return value;
	}

	void setValue(T value) {
		this.value = value;
	}

}
