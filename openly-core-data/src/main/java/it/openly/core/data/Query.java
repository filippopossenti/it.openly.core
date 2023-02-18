package it.openly.core.data;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Represents a configured query, ready for execution. Provides facilities to execute the query and obtain results in
 * the desired format.
 *
 * @author filippo.possenti
 */
@Slf4j
public class Query {

	private final String sqlStatement;
	private final Map<String, ?> context;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public Query(@NonNull NamedParameterJdbcTemplate namedParameterJdbcTemplate, @NonNull String sqlStatement, Map<String, ?> context) {
		this.sqlStatement = sqlStatement;
		this.context = context;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	/**
	 * Executes the query, using the specified callback to handle the results
	 * @param callback The callback that will handle the results.
	 */
	public final void query(RowCallbackHandler callback) {
		namedParameterJdbcTemplate.query(sqlStatement, context, callback);
	}

	/**
	 * Executes the query expecting a list of {@link java.util.Map Map<String, Object>} as a result
	 * @return A list of {@link java.util.Map Map<String, Object>} objects.
	 */
	public final List<Map<String, Object>> queryForList() {
		return namedParameterJdbcTemplate.queryForList(sqlStatement, context);
	}

	/**
	 * Executes a query and returns a list of beans of the specified type.<br/>
	 * The columns are converted according to spring's {@link org.springframework.jdbc.core.BeanPropertyRowMapper BeanPropertyRowMapper} rules.<br/>
	 * It's important to remember that this framework is not meant to handle relationships, meaning that the bean is expected
	 * to contain only primitive (eventually boxed) types.<br/>
	 * @param clazz The bean's class.
	 * @param <T> The type.
	 * @return A list of beans.
	 */
	public final <T> List<T> queryForBeans(Class<T> clazz) {
		return namedParameterJdbcTemplate.query(sqlStatement, new MapSqlParameterSource(context), new BeanPropertyRowMapper<>(clazz));
	}

	/**
	 * Executes a query and returns an integer
	 * @return An integer value
	 */
	public final int queryForInt() {
		return queryForObject(Integer.class);
	}

	/**
	 * Executes a query and returns a long
	 * @return A long value
	 */
	public final long queryForLong() {
		return queryForObject(Long.class);
	}

	/**
	 * Executes a query and returns an object. Note that in this case the object is expected to be
	 * a primitive (possibly boxed) java type. Beans are not allowed.<br/>
	 * @param clazz The object type
	 * @param <T> The type
	 * @return The object
	 */
	public final <T> T queryForObject(Class<T> clazz) {
		return namedParameterJdbcTemplate.queryForObject(sqlStatement, context, clazz);
	}

	/**
	 * Executes a query and returns a {@link java.util.Map Map<String, Object>}.
	 * @return A {@link java.util.Map Map<String, Object>} object
	 */
	public final Map<String, Object> queryForMap() {
		return namedParameterJdbcTemplate.queryForMap(sqlStatement, context);
	}

	/**
	 * Executes a query and returns a bean of the specified type.<br/>
	 * The columns are converted according to spring's {@link org.springframework.jdbc.core.BeanPropertyRowMapper BeanPropertyRowMapper} rules.<br/>
	 * It's important to remember that this framework is not meant to handle relationships, meaning that the bean is expected
	 * to contain only primitive (eventually boxed) types.<br/>
	 * @param clazz The bean's class.
	 * @param <T> The type.
	 * @return A bean.
	 */
	public final <T> T queryForBean(Class<T> clazz) {
		return namedParameterJdbcTemplate.queryForObject(sqlStatement, new MapSqlParameterSource(context), new BeanPropertyRowMapper<>(clazz));
	}

	/**
	 * Executes an update query and returns the number of affected rows.
	 * @return The number of affected rows.
	 */
	public final int update() {
		return namedParameterJdbcTemplate.update(sqlStatement, context);
	}

	/**
	 * Executes a statement.
	 * @return A boolean
	 */
	public final boolean execute() {
		return execute(PreparedStatement::execute);
	}

	/**
	 * Executes a statement using the provided callback to drive execution. Don't forget to call the
	 * {@link PreparedStatement#execute() PreparedStatement.execute} method.
	 * @param preparedStatementCallback The callback
	 * @param <T> The result type
	 * @return The execution's result
	 */
	public final <T> T execute(PreparedStatementCallback<T> preparedStatementCallback) {
		return namedParameterJdbcTemplate.execute(sqlStatement, context, preparedStatementCallback);
	}

	/**
	 * Executes a query and iterates through the ResultSet executing the specified callback for
	 * each row. This is useful for large datasets to avoid keeping data in memory while it's
	 * being processed.
	 * @param callback The callback
	 */
	public final void iterate(final IRowHandlerCallback<Map<String, Object>> callback) {
		query(new RowCallbackHandler() {
			final ColumnMapRowMapper mapper = new ColumnMapRowMapper();

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				callback.handleRow(mapper.mapRow(rs, rs.getRow()));
			}
		});
	}

	/**
	 * Executes a query and iterates through the ResultSet executing the specified callback for
	 * each row. This is useful for large datasets to avoid keeping data in memory while it's
	 * being processed.
	 * @param clazz The class of the beans for processing
	 * @param callback The callback
	 * @param <T> The class of the pojo bean that will be used when iterating
	 */
	public final <T> void iterate(Class<T> clazz, final IRowHandlerCallback<T> callback) {
		query(new RowCallbackHandler() {
			final BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(clazz);

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				callback.handleRow(mapper.mapRow(rs, rs.getRow()));
			}
		});
	}

}
