package it.openly.core.utils;

import it.openly.core.exceptions.WrongArgumentsNumberException;

import java.util.HashMap;
import java.util.Map;

public class MapUtils implements IMapUtils {
	@Override
	public Map<String, Object> merge(Map<String, ?>[] contexts) {
		Map<String, Object> rv = new HashMap<>();
		for (int i = 0; i < contexts.length; i++) {
			rv.putAll(contexts[i]);
		}
		return rv;
	}

	@Override
	public Map<String, Object> mergeInto(Map<String, Object> destination, Map<String, ?>[] contexts) {
		for (int i = 0; i < contexts.length; i++) {
			if(contexts[i] != null) {
				destination.putAll(contexts[i]);
			}
		}
		return destination;
	}

	public Map<String, Object> build(Object... keysAndValues) {
		Map<String, Object> result = new HashMap<>();
		if (keysAndValues.length % 2 != 0) {
			throw new WrongArgumentsNumberException("Arguments must be even. The even-index argument is the key, the odd-index argument is the value.", keysAndValues.length);
		}
		for (int i = 0; i < keysAndValues.length; i += 2) {
			String key = (String) keysAndValues[i];
			Object value = keysAndValues[i + 1];
			result.put(key, value);
		}
		return result;
	}

}
