package it.openly.core.resources;

import java.io.InputStream;

public interface IResourceResolver {
	/**
	 * Resolves a resource for use by the application.
	 * @param resourceName
	 * @return
	 */
	InputStream resolveResource(String resourceName);
	
	boolean getFailIfNotExisting();
	void setFailIfNotExisting(boolean failIfNotExisting);
	
	boolean getFailIfManyExisting();
	void setFailIfManyExisting(boolean failIfManyExisting);
}
