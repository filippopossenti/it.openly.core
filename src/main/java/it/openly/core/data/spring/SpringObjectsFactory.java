package it.openly.core.data.spring;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

class SpringObjectsFactory {
    NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    ColumnMapRowMapper getColumnMapRowMapper() {
        return new ColumnMapRowMapper();
    }

}
