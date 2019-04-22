package it.openly.core.data.spring;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public class SpringObjectsFactory {
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    public ColumnMapRowMapper getColumnMapRowMapper() {
        return new ColumnMapRowMapper();
    }

}
