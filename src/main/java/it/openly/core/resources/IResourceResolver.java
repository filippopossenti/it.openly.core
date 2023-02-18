package it.openly.core.resources;

import java.io.InputStream;

public interface IResourceResolver {

	boolean hasResource(String resourceName);

	/**
	 * Resolves a resource for use by the application.
	 * @param resourceName
	 * @return
	 */
	InputStream resolveResource(String resourceName);

	String resolveStringResource(String resourceName);

	boolean isFailIfNotExisting();
	void setFailIfNotExisting(boolean failIfNotExisting);
	
	boolean isFailIfManyExisting();
	void setFailIfManyExisting(boolean failIfManyExisting);
}
