package it.openly.core.resources;

import it.openly.core.exceptions.ResourceNotFoundException;
import it.openly.core.exceptions.TooFewResultsException;
import it.openly.core.exceptions.TooManyResultsException;

import java.io.InputStream;
import java.nio.charset.Charset;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Default implementation of IResourceResolver. It will either use the supplied
 * {@link ResourcePatternResolver} or a default
 * {@link PathMatchingResourcePatternResolver}
 * 
 * @author Filippo
 * 
 */
@Getter
@Setter
public class DefaultResourceResolver implements IResourceResolver {
	private String basePath = "classpath*:/";
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private boolean failIfNotExisting = true;
	private boolean failIfManyExisting = true;

	@SneakyThrows
	public boolean hasResource(String resourceName) {
		ResourcePatternResolver resolver = getResourcePatternResolver();
		String path = composePath(resourceName);
		Resource[] resources = resolver.getResources(path);
		return resources.length > 0;
	}

	@Override
	@SneakyThrows
	public InputStream resolveResource(String resourceName) {
		ResourcePatternResolver resolver = getResourcePatternResolver();
		String path = composePath(resourceName);
		Resource[] resources = resolver.getResources(path);
		if (resources.length == 0 && failIfNotExisting)
			throw new TooFewResultsException("No matching resource was found.", path);
		else if (resources.length > 1 && failIfManyExisting)
			throw new TooManyResultsException("Too many resources found for the given path.", path);
		if (resources.length == 0)
			return null;
		if (!resources[0].exists())
			throw new ResourceNotFoundException("A resource was supposed to exist and yet it didn't.", path);
		return resources[0].getInputStream();
	}

	@Override
	@SneakyThrows
	public String resolveStringResource(String resourceName) {
		return IOUtils.toString(resolveResource(resourceName), Charset.defaultCharset());
	}

	private String composePath(String resourceName) {
		String result = getBasePath();
		if (!StringUtils.endsWith(result, "/"))
			result += "/";
		result += resourceName;
		result = result.replace('\\', '/');
		return result;
	}
}
