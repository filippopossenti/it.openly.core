package it.openly.core.data.spring;

import it.openly.core.data.AbstractQuery;
import it.openly.core.data.IDataSourceAware;
import it.openly.core.data.IRowHandlerCallback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Run queries.
 * 
 * @author Filippo
 * 
 */
@SuppressWarnings({"unchecked", "varargs"})
public class SpringQuery extends AbstractQuery implements IDataSourceAware {

	private DataSource dataSource;

	private SpringObjectsFactory springObjectsFactory;

	public SpringQuery(DataSource dataSource, String sqlStatement, Map<String, ?> context, SpringObjectsFactory springObjectsFactory) {
		super(sqlStatement, context);
		this.dataSource = dataSource;
		this.springObjectsFactory = springObjectsFactory;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource value) {
		dataSource = value;
	}

	public void query(DataSource dataSource, RowCallbackHandler callback, Map<String, ?>... contexts) {
		springObjectsFactory.getNamedParameterJdbcTemplate(dataSource).query(getSqlStatement(), mergeContext(contexts), callback);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryForList(Map<String, ?>... contexts) {
		return (List<T>)springObjectsFactory.getNamedParameterJdbcTemplate(getDataSource()).queryForList(getSqlStatement(), mergeContext(contexts));
	}

	@Override
	public int queryForInt(Map<String, ?>... contexts) {
		return queryForObject(Integer.class, contexts);
	}

	@Override
	public long queryForLong(Map<String, ?>... contexts) {
		return queryForObject(Long.class, contexts);
	}

	@Override
	public <T> T queryForObject(Class<T> clazz, Map<String, ?>... contexts) {
		return springObjectsFactory.getNamedParameterJdbcTemplate(getDataSource()).queryForObject(getSqlStatement(), mergeContext(contexts), clazz);
	}

	@Override
	public Map<String, Object> queryForMap(Map<String, ?>... contexts) {
		return springObjectsFactory.getNamedParameterJdbcTemplate(getDataSource()).queryForMap(getSqlStatement(), mergeContext(contexts));
	}

	@Override
	public int update(Map<String, ?>... contexts) {
		return springObjectsFactory.getNamedParameterJdbcTemplate(getDataSource()).update(getSqlStatement(), mergeContext(contexts));
	}

	@Override
	public void execute(Map<String, ?>... contexts) {
		springObjectsFactory.getNamedParameterJdbcTemplate(getDataSource()).execute(getSqlStatement(), mergeContext(contexts), var1 -> null);
	}

	@Override
	public <T> void iterate(final IRowHandlerCallback<T> callback, Map<String, ?>... contexts) {
		query(getDataSource(), new RowCallbackHandler() {
			ColumnMapRowMapper mapper = springObjectsFactory.getColumnMapRowMapper();

			@SuppressWarnings("unchecked")
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				callback.handleRow((T) mapper.mapRow(rs, rs.getRow()));
			}
		}, mergeContext(contexts));
	}
}
