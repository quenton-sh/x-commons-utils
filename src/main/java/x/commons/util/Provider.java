package x.commons.util;

import java.util.Map;

public interface Provider<T> {

	public T get();
	
	public T get(Object ...args);
	
	public T get(Map<String, Object> args);
}
