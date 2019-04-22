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
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpringQueryTest {

    private static final String SELECT_FROM_MYTABLE_WHERE_X_X = "select * from mytable where x=:x";
    private static final String UPDATE_MYTABLE_SET_X_Y_WHERE_X_X = "update mytable set x=:y where x=:x";

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
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);

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
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(springObjectsFactory.getColumnMapRowMapper()).thenReturn(columnMapRowMapper);
        when(columnMapRowMapper.mapRow(eq(resultSet), eq(1))).thenReturn(row0);
        when(resultSet.getRow()).thenReturn(1);
        doAnswer(a -> {
            RowCallbackHandler rch = a.getArgument(2);
            rch.processRow(resultSet);
            return null;
        }).when(namedParameterJdbcTemplate).query(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap(), any(RowCallbackHandler.class));

        // when
        query.iterate(rowHandlerCallback, new HashMap<>());

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).query(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap(), any(RowCallbackHandler.class));
        verify(rowHandlerCallback, times(1)).handleRow(row0);
    }

    @Test
    public void testExecuteCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, UPDATE_MYTABLE_SET_X_Y_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);

        // when
        query.execute(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).execute(eq(UPDATE_MYTABLE_SET_X_Y_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))), any());
    }

    @Test
    public void testUpdateCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, UPDATE_MYTABLE_SET_X_Y_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);

        // when
        query.update(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).update(eq(UPDATE_MYTABLE_SET_X_Y_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))));
    }

    @Test
    public void testQueryForMapCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(namedParameterJdbcTemplate.queryForMap(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap())).thenReturn(result);

        // when
        Map<String, Object> actualResult = query.queryForMap(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).queryForMap(eq(SELECT_FROM_MYTABLE_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))));
        assertThat(actualResult, is(result));
    }

    @Test
    public void testQueryForLongCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        long result = (long)(Math.random() * (double)(Long.MAX_VALUE - 1));
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(namedParameterJdbcTemplate.queryForObject(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap(), eq(Long.class))).thenReturn(result);

        // when
        long actualResult = query.queryForLong(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(eq(SELECT_FROM_MYTABLE_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))), eq(Long.class));
        assertThat(actualResult, is(result));
    }

    @Test
    public void testQueryForIntCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        int result = (int)(Math.random() * (double)(Integer.MAX_VALUE - 1));
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(namedParameterJdbcTemplate.queryForObject(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap(), eq(Integer.class))).thenReturn(result);

        // when
        int actualResult = query.queryForInt(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(eq(SELECT_FROM_MYTABLE_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))), eq(Integer.class));
        assertThat(actualResult, is(result));
    }

    @Test
    public void testQueryForListCallsSpring() {
        // given
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        context.put("y", UUID.randomUUID().toString());
        params.put("x", UUID.randomUUID().toString());
        SpringQuery query = new SpringQuery(dataSource1, SELECT_FROM_MYTABLE_WHERE_X_X, context, springObjectsFactory);
        when(springObjectsFactory.getNamedParameterJdbcTemplate(dataSource1)).thenReturn(namedParameterJdbcTemplate);
        when(namedParameterJdbcTemplate.queryForList(eq(SELECT_FROM_MYTABLE_WHERE_X_X), anyMap())).thenReturn(results);

        // when
        List<Map<String, Object>> actualResults = query.queryForList(params);

        // then
        verify(springObjectsFactory, times(1)).getNamedParameterJdbcTemplate(dataSource1);
        verify(namedParameterJdbcTemplate, times(1)).queryForList(eq(SELECT_FROM_MYTABLE_WHERE_X_X), argThat((Map<String, Object> m) -> Objects.equals(m.get("x"), params.get("x")) && Objects.equals(m.get("y"), context.get("y"))));
        assertThat(actualResults, is(results));
    }
}
