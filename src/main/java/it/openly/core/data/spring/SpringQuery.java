package it.openly.core.data.spring;

import it.openly.core.data.AbstractQuery;
import it.openly.core.data.IRowHandlerCallback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Run querys.
 * 
 * @author Filippo
 * 
 */
public class SpringQuery extends AbstractQuery {

	private DataSource dataSource = null;

	public SpringQuery(DataSource dataSource, String sqlStatement, Map<String, ?> context) {
		super(sqlStatement, context);
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource value) {
		dataSource = value;
	}

	public void query(DataSource dataSource, RowCallbackHandler callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		getNamedParameterJdbcTemplate(dataSource).query(getSqlStatement(), mergeContext(contexts), callback);
	}

	public void query(RowCallbackHandler callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		query(getDataSource(), callback, contexts);
	}

	public <T> void execute(DataSource dataSource, PreparedStatementCallback<T> callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		getNamedParameterJdbcTemplate(dataSource).execute(getSqlStatement(), mergeContext(contexts), callback);
	}

	public <T> void execute(PreparedStatementCallback<T> callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		execute(getDataSource(), callback, contexts);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
		assertNotNull(dataSource, "DataSource cannot be null.");
		return new NamedParameterJdbcTemplate(dataSource);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return getNamedParameterJdbcTemplate(getDataSource());
	}

	public List<Map<String, Object>> queryForList(DataSource dataSource, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return getNamedParameterJdbcTemplate(dataSource).queryForList(getSqlStatement(), mergeContext(contexts));
	}

	public int queryForInt(DataSource dataSource, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return queryForObject(dataSource, Integer.class, contexts);
	}

	public long queryForLong(DataSource dataSource, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return queryForObject(dataSource, Long.class, contexts);
	}

	public <T> T queryForObject(DataSource dataSource, Class<T> clazz, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return getNamedParameterJdbcTemplate(dataSource).queryForObject(getSqlStatement(), mergeContext(contexts), clazz);
	}

	public Map<String, Object> queryForMap(DataSource dataSource, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return getNamedParameterJdbcTemplate(dataSource).queryForMap(getSqlStatement(), mergeContext(contexts));
	}

	public int update(DataSource dataSource, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return getNamedParameterJdbcTemplate(dataSource).update(getSqlStatement(), mergeContext(contexts));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryForList(Map<String, ?>... contexts) {
		return (List<T>) queryForList(getDataSource(), contexts);
	}

	@Override
	public int queryForInt(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return queryForInt(getDataSource(), contexts);
	}

	@Override
	public long queryForLong(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return queryForLong(getDataSource(), contexts);
	}

	@Override
	public <T> T queryForObject(Class<T> clazz, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return queryForObject(getDataSource(), clazz, contexts);
	}

	@Override
	public int update(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return update(getDataSource(), contexts);
	}

	@Override
	public <T> void iterate(IRowHandlerCallback<T> callback, @SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		final IRowHandlerCallback<T> cb = callback;
		getNamedParameterJdbcTemplate(dataSource).query(getSqlStatement(), mergeContext(contexts), new RowCallbackHandler() {

			ColumnMapRowMapper mapper = new ColumnMapRowMapper();

			@SuppressWarnings("unchecked")
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				cb.handleRow((T) mapper.mapRow(rs, rs.getRow()));
			}

		});
	}

}
