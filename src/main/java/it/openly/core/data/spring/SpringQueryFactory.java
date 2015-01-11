package it.openly.core.data.spring;

import it.openly.core.data.AbstractQueryFactory;
import it.openly.core.data.ContextUtils;
import it.openly.core.data.IQuery;
import it.openly.core.data.ITransaction;

import java.util.Map;

import javax.sql.DataSource;

/**
 * Factory object for querys. Create query objects taking care of adding an
 * additional parsing layer using Velocity if needed.
 * 
 */
public class SpringQueryFactory extends AbstractQueryFactory {
	private DataSource dataSource = null;

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
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the datasource used by default when creating new querys.
	 * 
	 * @param value
	 */
	public void setDataSource(DataSource value) {
		dataSource = value;
	}

	@Override
	public ITransaction getTransaction() {
		return new SpringTransaction(getDataSource());
	}

	@Override
	protected IQuery createQueryObject(String sql, Map<String, ?> context, ContextUtils ctx) {
		return new SpringQuery(getDataSource(), sql, context);
	}
}
