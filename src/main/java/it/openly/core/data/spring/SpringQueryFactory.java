package it.openly.core.data.spring;

import it.openly.core.Maps;
import it.openly.core.data.AbstractQueryFactory;
import it.openly.core.data.ContextUtils;
import it.openly.core.data.IDataSourceAware;
import it.openly.core.data.IQuery;
import it.openly.core.data.ITransaction;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Factory object for queries. Create query objects taking care of adding an
 * additional parsing layer using Velocity if needed.
 * 
 */
public class SpringQueryFactory extends AbstractQueryFactory implements IDataSourceAware {
	private DataSource dataSource = null;
	private SpringObjectsFactory springObjectsFactory;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String> databaseTypes = (Map<String, String>)((Map)Maps.build(
		"Oracle", "oracle",
		"Microsoft SQL Server", "mssql",
		"PostgreSQL", "postgresql",
		"MySQL", "mysql",
		"HSQL Database Engine", "hsql"
	));
	private String dbType = null;
	
	private String detectDbType(DataSource ds) {
		if(dbType == null) {
			Map<String, String> dbTypes = getDatabaseTypes();
			Connection conn = null;
			try {
				conn = ds.getConnection();
				String dbProduct = conn.getMetaData().getDatabaseProductName();
				dbType = dbTypes.get(dbProduct);
				if(dbType == null) {
					dbType = "generic";
				}
			}
			catch(Exception ex) {
				throw new RuntimeException(ex);
			}
			finally {
				DataSourceUtils.releaseConnection(conn, ds);
			}
		}
		return dbType;
	}

	public SpringQueryFactory(DataSource dataSource, SpringObjectsFactory springObjectsFactory) {
		super();
		setDataSource(dataSource);
		setSpringObjectsFactory(springObjectsFactory);
	}

	public SpringQueryFactory(DataSource dataSource) {
		this(dataSource, new SpringObjectsFactory());
	}

	public SpringQueryFactory() {
		this(null, new SpringObjectsFactory());
	}
	
	/**
	 * Gets the datasource used by default when creating new queries.
	 * 
	 * @return The datasource
	 */
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the datasource used by default when creating new queries.
	 * 
	 * @param value The datasource
	 */
	@Override
	public void setDataSource(DataSource value) {
		dataSource = value;
		dbType = null;
	}
	
	/**
	 * Returns a collection of all known database types
	 * @return
	 */
	public Map<String, String> getDatabaseTypes() {
		return databaseTypes;
	}
	
	/**
	 * Sets the collection of all known database types
	 * @param value The database types. The key is the value fetched from the database, the value is a short string identifying the database that will be used to fetch queries
	 */
	public void setDatabaseTypes(Map<String, String> value) {
		this.databaseTypes = value;
	}

	/**
	 * Returns the factory of the NamedParameterJdbcTemplate objects
	 * @return The factory
	 */
	public SpringObjectsFactory setSpringObjectsFactory() {
		return springObjectsFactory;
	}

	/**
	 * Sets the factory of the NamedParameterJdbcTemplate objects
	 * @param springObjectsFactory The factory
	 */
	public void setSpringObjectsFactory(SpringObjectsFactory springObjectsFactory) {
		this.springObjectsFactory = springObjectsFactory;
	}

	@Override
	public ITransaction getTransaction() {
		return new SpringTransaction(getDataSource());
	}

	@Override
	protected IQuery createQueryObject(String sql, Map<String, ?> context, ContextUtils ctx) {
		return new SpringQuery(getDataSource(), sql, context, springObjectsFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IQuery createQuery(String namedQuery, Map<String, ?>... contexts) {
		return super.createQuery(buildQueryPath(namedQuery), contexts);
	}
	
	
	private String buildQueryPath(String namedQuery) {
		String detectedDbType = detectDbType(getDataSource());
		return FilenameUtils.concat(detectedDbType, namedQuery).replace('\\', '/');
	}
}
