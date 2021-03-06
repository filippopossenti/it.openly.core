package it.openly.core.data.support;

import it.openly.core.data.IQueryResourceLoader;
import it.openly.core.resources.DefaultResourceResolver;
import it.openly.core.resources.IResourceResolver;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import javax.sql.DataSource;

/**
 * This class allows loading queries from resources.<br/>
 * The path is composed based on a base path, combined with the database product as returned by the {@link DbProductDetector DbProductDetector} class.
 * When there is no database-specific query, the class will combine the base path with the string "sql" and will try to load the query from there.
 *
 * @author filippo.possenti
 */
public class DefaultQueryResourceLoader implements IQueryResourceLoader {

    private final IResourceResolver resourceResolver;
    private final DbProductDetector dbProductDetector;
    private final String queriesBasePath;

    /**
     * Creates an instance of the class, specifying all parameters that drive the resolution of the resource to be loaded.
     * @param resourceResolver The resource resolver that will be tasked with loading the resource.
     * @param dbProductDetector The {@link DbProductDetector DbProductDetector} in charge of detecting the database product for path composition.
     * @param queriesBasePath The base path to the queries.
     */
    public DefaultQueryResourceLoader(IResourceResolver resourceResolver, DbProductDetector dbProductDetector, String queriesBasePath) {
        this.resourceResolver = resourceResolver;
        this.dbProductDetector = dbProductDetector;
        this.queriesBasePath = queriesBasePath;
    }

    /**
     * Creates an instance of the class, configured as follows:
     * <ul>
     * <li>resources are loaded from the file-system, using the {@link DefaultResourceResolver DefaultResourceResolver} class.</li>
     * <li>the database product is determined using the default implementation and configuration of the {@link DbProductDetector DbProductDetector} class</li>
     * <li>the base path of the resources is a "queries" directory just underneath the classpath</li>
     * <li>each query resource will have an extension of ".sql"</li>
     * </ul>
     */
    public DefaultQueryResourceLoader() {
        this(new DefaultResourceResolver(), new DbProductDetector(), "queries");
    }

    /**
     * Loads the query using the configured dependencies.
     * @param dataSource The {@link javax.sql.DataSource DataSource} instance for which the query is to be loaded
     * @param namedQuery The name of the query
     * @return A string representing the text that composes the query, unmodified.
     */
    @Override
    public String loadQuery(@NonNull DataSource dataSource, @NonNull String namedQuery) {
        String detectedDbType = dbProductDetector.detectDbProduct(dataSource);
        String namedQueryPath = FilenameUtils.concat(FilenameUtils.concat(queriesBasePath, detectedDbType), namedQuery);
        if(!resourceResolver.hasResource(namedQueryPath)) {
            namedQueryPath = FilenameUtils.concat(FilenameUtils.concat(queriesBasePath, "sql"), namedQuery);
        }
        return resourceResolver.resolveStringResource(namedQueryPath);
    }
}
