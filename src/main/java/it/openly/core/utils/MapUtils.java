package it.openly.core.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils implements IMapUtils {
	@Override
	public Map<String, Object> merge(Map<String, ?>[] contexts) {
		Map<String, Object> rv = new HashMap<String, Object>();
		for (int i = 0; i < contexts.length; i++) {
			rv.putAll(contexts[i]);
		}
		return rv;
	}
}
