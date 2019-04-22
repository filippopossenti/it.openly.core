package it.openly.core.utils;

import java.util.Map;

public interface IMapUtils {
	Map<String, Object> merge(Map<String, ?>[] contexts);

	Map<String, Object> mergeInto(Map<String, Object> destination, Map<String, ?>[] contexts);

	Map<String, Object> build(Object... keysAndValues);
}
