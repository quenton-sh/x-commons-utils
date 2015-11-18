package x.commons.util.failover;

public interface FailoverCallable<RETURN_TYPE, RESOURCE_TYPE> {

	public RETURN_TYPE call(RESOURCE_TYPE resource, int resourceIndex) throws Exception;
}
