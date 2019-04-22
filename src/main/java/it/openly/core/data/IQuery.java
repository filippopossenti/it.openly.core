package it.openly.core.data;

import java.util.List;
import java.util.Map;

/**
 * A generic runnable query. Implementation may provide additional methods for
 * greater flexibility.
 * 
 * @author Filippo
 * 
 */
@SuppressWarnings({"unchecked", "varargs"})
public interface IQuery {

	/**
	 * Return the set of parameters used in the query.
	 * 
	 * @return
	 */
	Map<String, ?> getContext();

	/**
	 * Returns the pre-processed SQL statement.
	 * 
	 * @return
	 */
	String getSqlStatement();
	
	/**
	 * Returns a list of objects.
	 * 
	 * @param contexts
	 * 
	 * @return
	 */
	<T> List<T> queryForList(Map<String, ?>... contexts);

	int queryForInt(Map<String, ?>... contexts);

	long queryForLong(Map<String, ?>... contexts);

	<T> T queryForObject(Class<T> clazz, Map<String, ?>... contexts);
	
	Map<String, Object> queryForMap(Map<String, ?>... contexts);

	int update(Map<String, ?>... contexts);

	void execute(Map<String, ?>... contexts);

	/**
	 * Iterates through records. This method is intended to provide a way to
	 * iterate records one at a time instead of loading them all into memory.
	 * Use it if you need to handle a very high number of records.
	 */
	<T> void iterate(IRowHandlerCallback<T> callback, Map<String, ?>... contexts);
	
}