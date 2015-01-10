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

	public void query(DataSource dataSource, RowCallbackHandler callback) {
		getNamedParameterJdbcTemplate(dataSource).query(getSqlStatement(), getContext(), callback);
	}

	public void query(RowCallbackHandler callback) {
		query(getDataSource(), callback);
	}

	public <T> void execute(DataSource dataSource, PreparedStatementCallback<T> callback) {
		getNamedParameterJdbcTemplate(dataSource).execute(getSqlStatement(), getContext(), callback);
	}

	public <T> void execute(PreparedStatementCallback<T> callback) {
		execute(callback);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
		assertNotNull(dataSource, "DataSource cannot be null.");
		return new NamedParameterJdbcTemplate(dataSource);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return getNamedParameterJdbcTemplate(getDataSource());
	}

	public List<Map<String, Object>> queryForList(DataSource dataSource) {
		return getNamedParameterJdbcTemplate(dataSource).queryForList(getSqlStatement(), getContext());
	}

	public int queryForInt(DataSource dataSource) {
		return queryForObject(dataSource, Integer.class);
	}

	public long queryForLong(DataSource dataSource) {
		return queryForObject(dataSource, Long.class);
	}

	public <T> T queryForObject(DataSource dataSource, Class<T> clazz) {
		return getNamedParameterJdbcTemplate(dataSource).queryForObject(getSqlStatement(), getContext(), clazz);
	}

	public Map<String, Object> queryForMap(DataSource dataSource) {
		return getNamedParameterJdbcTemplate(dataSource).queryForMap(getSqlStatement(), getContext());
	}

	public int update(DataSource dataSource) {
		return getNamedParameterJdbcTemplate(dataSource).update(getSqlStatement(), getContext());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryForList() {
		return (List<T>) queryForList(getDataSource());
	}

	@Override
	public int queryForInt() {
		return queryForInt(getDataSource());
	}

	@Override
	public long queryForLong() {
		return queryForLong(getDataSource());
	}

	@Override
	public <T> T queryForObject(Class<T> clazz) {
		return queryForObject(getDataSource(), clazz);
	}

	@Override
	public int update() {
		return update(getDataSource());
	}

	@Override
	public <T> void iterate(IRowHandlerCallback<T> callback) {
		final IRowHandlerCallback<T> cb = callback;
		getNamedParameterJdbcTemplate(dataSource).query(getSqlStatement(), getContext(), new RowCallbackHandler() {

			ColumnMapRowMapper mapper = new ColumnMapRowMapper();

			@SuppressWarnings("unchecked")
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				cb.handleRow((T) mapper.mapRow(rs, rs.getRow()));
			}

		});
	}

}
