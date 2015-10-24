package it.openly.core.data.spring;

import it.openly.core.Maps;
import it.openly.core.data.AbstractQueryFactory;
import it.openly.core.data.ContextUtils;
import it.openly.core.data.IDataSourceAware;
import it.openly.core.data.IQuery;
import it.openly.core.data.IQueryFactory;
import it.openly.core.data.ITransaction;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;

/**
 * Factory object for queries. Create query objects taking care of adding an
 * additional parsing layer using Velocity if needed.
 * 
 */
public class SpringQueryFactory extends AbstractQueryFactory implements IDataSourceAware {
	private DataSource dataSource = null;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String> databaseTypes = (Map<String, String>)((Map)Maps.build(
		"Oracle", "oracle",
		"Microsoft SQL Server", "mssql",
		"PostgreSQL", "postgresql",
		"MySQL", "mysql",
		"HSQL Database Engine", "hsql"
	));

	public SpringQueryFactory() {
		super();
	}
	
	public SpringQueryFactory(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}

	/**
	 * Gets the datasource used by default when creating new querys.
	 * 
	 * @return
	 */
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the datasource used by default when creating new querys.
	 * 
	 * @param value
	 */
	@Override
	public void setDataSource(DataSource value) {
		dataSource = value;
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
	 * @param value
	 */
	public void setDatabaseTypes(Map<String, String> value) {
		this.databaseTypes = value;
	}
	
	@Override
	public ITransaction getTransaction() {
		return new SpringTransaction(getDataSource());
	}

	@Override
	protected IQuery createQueryObject(String sql, Map<String, ?> context, ContextUtils ctx) {
		return new SpringQuery(getDataSource(), sql, context);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IQuery createQuery(String namedQuery, Map<String, ?>... contexts) {
		return super.createQuery(buildQueryPath(namedQuery), contexts);
	}
	
	
	private String buildQueryPath(String namedQuery) {
		try {
			DataSource ds = getDataSource();
			Map<String, String> databaseTypes = getDatabaseTypes();
			String dbProduct = ds.getConnection().getMetaData().getDatabaseProductName();
			String dbType = databaseTypes.get(dbProduct);
			if(dbType == null) {
				dbType = "generic";
			}
			return FilenameUtils.concat(dbType, namedQuery).replace('\\', '/');
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
