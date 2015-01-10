package it.openly.core;

import it.openly.core.utils.IMapUtils;
import it.openly.core.utils.MapUtils;

import java.util.Map;

/**
 * Shorthand for {@link it.openly.core.utils.MapUtils}. This approach allow
 * replacing the default implementation with a custom one in case of need.
 * 
 * @author Filippo
 * 
 */
public class Maps {
	private static IMapUtils mapUtils = new MapUtils();

	public static void setMapUtils(IMapUtils value) {
		mapUtils = value;
	}

	@SafeVarargs
	public static Map<String, Object> merge(Map<String, ?>... contexts) {
		return mapUtils.merge(contexts);
	}

	@SafeVarargs
	public static Map<String, Object> mergeInto(Map<String, Object> destination, Map<String, ?>... contexts) {
		return mapUtils.mergeInto(destination, contexts);
	}

}
