package it.openly.core.utils;

import java.util.Map;

public interface IMapUtils {
	public Map<String, Object> merge(Map<String, ?>[] contexts);

	public Map<String, Object> mergeInto(Map<String, Object> destination, Map<String, ?>[] contexts);

	public Map<String, Object> build(Object... keysAndValues);
}
