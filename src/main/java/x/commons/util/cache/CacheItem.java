package x.commons.util.cache;

public class CacheItem<T> {

	private Long expireTs;
	private T value;

	public Long getExpireTs() {
		return expireTs;
	}

	public void setExpireTs(Long expireTs) {
		this.expireTs = expireTs;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
