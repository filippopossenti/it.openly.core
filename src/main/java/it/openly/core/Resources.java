package it.openly.core;

import it.openly.core.exceptions.AppRuntimeException;
import it.openly.core.exceptions.ResourceNotFoundException;
import it.openly.core.resources.DefaultResourceResolver;
import it.openly.core.resources.IResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Utilities to load resources for application use.
 * 
 * @author Filippo
 * 
 */
public class Resources {
	private static List<IResourceResolver> resourceResolvers;

	static {
		IResourceResolver defaultResolver = new DefaultResourceResolver();
		defaultResolver.setFailIfNotExisting(true);
		defaultResolver.setFailIfManyExisting(true);
		resourceResolvers = new ArrayList<>();
		resourceResolvers.add(defaultResolver);
	}

	public static List<IResourceResolver> getResourceResolvers() {
		return resourceResolvers;
	}

	public static void setResourceResolvers(List<IResourceResolver> value) {
		resourceResolvers = value;
	}

	public static InputStream resolveResource(String resourceName) {
		return resolveResource(resourceResolvers, resourceName);
	}

	public static String resolveStringResource(String resourceName) {
		return resolveStringResource(resourceResolvers, resourceName);
	}

	public static InputStream resolveResource(List<IResourceResolver> resolvers, String resourceName) {
		for (int i = 0; i < resolvers.size(); i++) {
			IResourceResolver r = resolvers.get(i);
			InputStream stream = r.resolveResource(resourceName);
			if (stream != null)
				return stream;
		}
		throw new ResourceNotFoundException("Could not find specified resource.", resourceName);
	}

	public static String resolveStringResource(List<IResourceResolver> resolvers, String resourceName) {
		try {
			return IOUtils.toString(resolveResource(resolvers, resourceName));
		} catch (IOException e) {
			throw new AppRuntimeException("Could not read specified resource", e, resourceName);
		}
	}
}
