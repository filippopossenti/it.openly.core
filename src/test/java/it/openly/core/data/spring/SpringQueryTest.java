package it.openly.core.data.spring;

import it.openly.core.data.IRowHandlerCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpringQueryTest {

    @Mock
    DataSource dataSource1;

    @Mock
    DataSource dataSource2;

    @Mock
    SpringObjectsFactory springObjectsFactory;

    @Mock
    IRowHandlerCallback<Object> rowHandlerCallback;

    @Mock
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    ResultSet resultSet;

    @Mock
    ColumnMapRowMapper columnMapRowMapper;

    @Test
    public void testDataSourceCorrectlySet() {
        // given
        Map<String, Object> context = new HashMap<>();
        SpringQuery query = new SpringQuery(dataSource1, "select * from mytable where x=:x", context, springObjectsFactory);

        // when
        DataSource actualDataSource1 = query.getDataSource();
        query.setDataSource(dataSource2);
        DataSource actualDataSource2 = query.getDataSource();

        // then
        assertThat(actualDataSource1, is(dataSource1));
        assertThat(actualDataSource2, is(dataSource2));
    }

    @Test
    public void testIterateCallsSpring() throws SQLException {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> row0 = new HashMap<>();
        SpringQuery query = new SpringQuery(dataSource1, "select * from mytable where x=:x", context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(springObjectsFactory.getColumnMapRowMapper()).thenReturn(columnMapRowMapper);
        when(columnMapRowMapper.mapRow(eq(resultSet), eq(1))).thenReturn(row0);
        when(resultSet.getRow()).thenReturn(1);
        doAnswer(a -> {
            RowCallbackHandler rch = a.getArgument(2);
            rch.processRow(resultSet);
            return null;
        }).when(namedParameterJdbcTemplate).query(eq("select * from mytable where x=:x"), anyMap(), any(RowCallbackHandler.class));

        // when
        query.iterate(rowHandlerCallback, new HashMap<>());

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).query(eq("select * from mytable where x=:x"), anyMap(), any(RowCallbackHandler.class));
        verify(rowHandlerCallback, times(1)).handleRow(row0);
    }

    @Test
    public void testExecuteCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        context.put("y", "y-value");
        params.put("x", "x-value");
        SpringQuery query = new SpringQuery(dataSource1, "update mytable set x=:y where x=:x", context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);

        // when
        query.execute(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).execute(eq("update mytable set x=:y where x=:x"), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))), any());
    }

    @Test
    public void testUpdateCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        context.put("y", "y-value");
        params.put("x", "x-value");
        SpringQuery query = new SpringQuery(dataSource1, "update mytable set x=:y where x=:x", context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);

        // when
        query.update(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).update(eq("update mytable set x=:y where x=:x"), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))));
    }

    @Test
    public void testQueryForMapCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        context.put("y", "y-value");
        params.put("x", "x-value");
        SpringQuery query = new SpringQuery(dataSource1, "select * from mytable where x=:x", context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(namedParameterJdbcTemplate.queryForMap(eq("select * from mytable where x=:x"), anyMap())).thenReturn(result);

        // when
        Map<String, Object> actualResult = query.queryForMap(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).queryForMap(eq("select * from mytable where x=:x"), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))));
        assertThat(actualResult, is(result));
    }

}
