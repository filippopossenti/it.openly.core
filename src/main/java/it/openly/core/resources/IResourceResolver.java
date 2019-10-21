package it.openly.core.resources;

import java.io.InputStream;

public interface IResourceResolver {
	/**
	 * Resolves a resource for use by the application.
	 * @param resourceName
	 * @return
	 */
	InputStream resolveResource(String resourceName);

	String resolveStringResource(String resourceName);

	Boolean getFailIfNotExisting();
	void setFailIfNotExisting(Boolean failIfNotExisting);
	
	Boolean getFailIfManyExisting();
	void setFailIfManyExisting(Boolean failIfManyExisting);
}
