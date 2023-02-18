package it.openly.core.data.support;

import it.openly.core.data.QueryFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Provides facilities to detect the database product that is backing a specific {@link javax.sql.DataSource DataSource}.<br/>
 * A default list of database products is provided within the library. Alternatively, a custom list can be specified
 * using the appropriate constructor.
 *
 * @author filippo.possenti
 */
@Slf4j
public class DbProductDetector {
    private static final String DEFAULT_PROPERTIES_PATH = "it/openly/core/data/database-types.properties";

    private final Map<DataSource, String> dataSourceDbTypesCache;

    @Getter
    private final Map<String, String> databaseTypes;

    /**
     * Creates an instance of the object, allowing to specify a set of properties representing the list of known
     * database products.
     * The key of the property is the alias that will be returned by the {@link #detectDbProduct detectDbProduct} method, whereas the value is
     * what the database returns as part of the {@link java.sql.DatabaseMetaData#getDatabaseProductName java.sql.DatabaseMetaData.getDatabaseProductName} call.
     * The results are cached for each different DataSource indefinitely.
     * @param databaseTypesProps The {@link java.util.Properties Properties} object containing the list of database products
     */
    public DbProductDetector(Properties databaseTypesProps) {
        this.databaseTypes = loadDatabaseProducts(databaseTypesProps);
        this.dataSourceDbTypesCache = new HashMap<>();
    }

    /**
     * Creates an instance of the object. The list of known database products is fetched from a file embedded in the
     * library. If this is not suitable, use the other available constructor.
     */
    public DbProductDetector() {
        this.databaseTypes = loadDatabaseProducts(loadDefaultProperties());
        this.dataSourceDbTypesCache = new HashMap<>();
    }

    /**
     * Detects the database product for the specified DataSource.
     * @param ds The  {@link javax.sql.DataSource DataSource} for which the database product is to be detected
     * @return The detected database product. Returns "generic" if the product is not known to the library.
     */
    @SneakyThrows
    public String detectDbProduct(@NonNull DataSource ds) {
        String dbType = dataSourceDbTypesCache.get(ds);
        if(dbType == null) {
            Map<String, String> dbTypes = getDatabaseTypes();
            try(Connection conn = ds.getConnection()) {
                String dbProduct = conn.getMetaData().getDatabaseProductName();
                dbType = dbTypes.get(dbProduct);
                if(dbType == null) {
                    dbType = "generic";
                }
            }
            dataSourceDbTypesCache.put(ds, dbType);
        }
        log.info("Reporting detected database of type '{}'.", dbType);
        return dbType;
    }

    private Map<String, String> loadDatabaseProducts(Properties props) {
        Map<String, String> results = new HashMap<>();
        props.forEach((key, value) -> results.put((String)value, (String)key));
        return results;
    }

    @SneakyThrows
    private Properties loadDefaultProperties() {
        Properties props = new Properties();
        try(InputStream fileData = QueryFactory.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_PATH)) {
            props.load(fileData);
        }
        return props;
    }

}
