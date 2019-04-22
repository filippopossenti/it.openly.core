package it.openly.core.data;

import it.openly.core.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract query. Provide some common features for specialized implementations.
 * 
 * @author Filippo
 * 
 */
public abstract class AbstractQuery implements IQuery {

	private String sqlStatement;
	private Map<String, ?> context;

	@Override
	public Map<String, ?> getContext() {
		return context;
	}

	@Override
	public String getSqlStatement() {
		return sqlStatement;
	}

	protected Map<String, ?> mergeContext(Map<String, ?>[] contexts) {
		return Maps.mergeInto(new HashMap<>(getContext()), contexts);
	}

	public AbstractQuery(String sqlStatement, Map<String, ?> context) {
		this.sqlStatement = sqlStatement;
		this.context = context;
	}

}
