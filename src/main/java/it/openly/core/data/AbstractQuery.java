package it.openly.core.data;

import java.util.Map;

/**
 * Abstract query. Provide some common features for specialized implementations.
 * @author Filippo
 *
 */
public abstract class AbstractQuery implements IQuery {

	private String sqlStatement;
	private Map<String, ?> context;

	@Override
	public Map<String, ?> getContext() { return context; }
	@Override
	public String getSqlStatement() { return sqlStatement; }

	protected void assertNotNull(Object obj, String message) {
		if(obj == null)
			throw new RuntimeException(message);
	}

	public AbstractQuery(String sqlStatement, Map<String, ?> context) {
		this.sqlStatement = sqlStatement;
		this.context = context;
	}
	
}
