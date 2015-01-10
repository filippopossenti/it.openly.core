package it.openly.core;

import it.openly.core.utils.IStringUtils;
import it.openly.core.utils.StringUtils;

/**
 * Shorthand for {@link it.openly.core.utils.StringUtils}. This approach
 * allow replacing the default implementation with a custom one in case of need.
 * 
 * @author Filippo
 * 
 */
public class Strings {
	private static IStringUtils stringUtils = new StringUtils();

	public static void setStringUtils(IStringUtils value) {
		stringUtils = value;
	}

	public static String removeEmptyLines(String instr) {
		return stringUtils.removeEmptyLines(instr);
	}

}
