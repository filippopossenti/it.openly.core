package it.openly.core.resources;

import it.openly.core.exceptions.ResourceNotFoundException;
import it.openly.core.exceptions.TooFewResultsException;
import it.openly.core.exceptions.TooManyResultsException;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Default implementation of IResourceResolver. It will either use the supplier
 * {@link ResourcePatternResolver} or a default
 * {@link PathMatchingResourcePatternResolver}
 * 
 * @author Filippo
 * 
 */
public class DefaultResourceResolver implements IResourceResolver {

	private String basePath = "classpath*:/";
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private boolean failIfNotExisting = false;
	private boolean failIfManyExisting = true;

	public void setResourcePatternResolver(ResourcePatternResolver value) {
		resourcePatternResolver = value;
	}

	public ResourcePatternResolver getResourcePatternResolver() {
		return resourcePatternResolver;
	}

	/**
	 * The path that will be prepended to all resources in order to locate them
	 * 
	 * @param value
	 */
	public void setBasePath(String value) {
		basePath = value;
	}

	/**
	 * The path that will be prepended to all resources in order to locate them
	 */
	public String getBasePath() {
		return basePath;
	}

	@Override
	public InputStream resolveResource(String resourceName) {
		return resolveResource(resourceName, getFailIfNotExisting(), getFailIfManyExisting());
	}
	
	@Override
	public InputStream resolveResource(String resourceName, boolean failIfNotExisting, boolean failIfManyExisting) {
		ResourcePatternResolver resolver = getResourcePatternResolver();
		String path = composePath(resourceName);
		try {
			Resource[] resources = resolver.getResources(path);
			if (resources.length == 0 && failIfNotExisting)
				throw new TooFewResultsException("No matching resource was not found.", path);
			else if (resources.length > 1 && failIfManyExisting)
				throw new TooManyResultsException("Too many resources found for the given path.", path);
			if (resources.length == 0)
				return null;
			if (!resources[0].exists())
				throw new ResourceNotFoundException("A resource was supposed to exist and yet it didn't.", path);
			return resources[0].getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String composePath(String resourceName) {
		String result = getBasePath();
		if (!StringUtils.endsWith(result, "/"))
			result += "/";
		result += resourceName;
		return result;
	}

	@Override
	public boolean getFailIfNotExisting() {
		return failIfNotExisting;
	}

	@Override
	public void setFailIfNotExisting(boolean failIfNotExisting) {
		this.failIfNotExisting = failIfNotExisting;
	}

	@Override
	public boolean getFailIfManyExisting() {
		return failIfManyExisting;
	}

	@Override
	public void setFailIfManyExisting(boolean failIfManyExisting) {
		this.failIfManyExisting = failIfManyExisting;
	}

}
