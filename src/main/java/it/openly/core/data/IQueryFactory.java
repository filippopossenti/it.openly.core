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
	 * @param namedQuery
	 * @param contexts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	IQuery createQuery(String namedQuery, Map<String, ?>... contexts);

	ITransaction getTransaction();
	
	<T> List<T> queryForList(String namedQuery, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
	int queryForInt(String namedQuery, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
	long queryForLong(String namedQuery, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
	<T> T queryForObject(String namedQuery, Class<T> clazz, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
	int update(String namedQuery, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
	<T> void iterate(String namedQuery, IRowHandlerCallback<T> callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts);
	
}