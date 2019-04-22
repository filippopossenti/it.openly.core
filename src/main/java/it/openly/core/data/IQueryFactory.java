package it.openly.core.data;

import java.util.List;
import java.util.Map;

/**
 * A generic query factory. Specific implementation may provide additional methods for greater flexibility.
 * @author Filippo
 *
 */
public interface IQueryFactory {
	
	/**
	 * Creates a query, eventually pre-processing it using the supplied contexts.
	 * A variable number of contexts is allowed in order to enable separating things based on their meaning. For example it may be useful to put all paging parameters in a Map and parameters specific for the query in another Map.
	 * @param namedQuery The name of the query
	 * @param contexts The data to be passed to the query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	IQuery createQuery(String namedQuery, Map<String, ?>... contexts);

	@SuppressWarnings("unchecked")
	IQuery createQueryFromTemplate(String sql, Map<String, ?>... contexts);
	
	ITransaction getTransaction();
	
	<T> List<T> queryForList(String namedQuery, Map<String, ?>... contexts);
	
	int queryForInt(String namedQuery, Map<String, ?>... contexts);
	
	long queryForLong(String namedQuery, Map<String, ?>... contexts);
	
	<T> T queryForObject(String namedQuery, Class<T> clazz, Map<String, ?>... contexts);
	
	int update(String namedQuery, Map<String, ?>... contexts);
	
	<T> void iterate(String namedQuery, IRowHandlerCallback<T> callback, Map<String, ?>... contexts);
}