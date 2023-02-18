package it.openly.core.data;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.*;

import static it.openly.core.test.TestUtils.dt;
import static it.openly.core.test.TestUtils.map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryTest {

    @Mock
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static DataSource dataSource;

    private final Map<String, Object> context = new HashMap<>();

    private static final List<Map<String, Object>> people = new ArrayList<>();

    @BeforeAll
    @SneakyThrows
    public static void prepareDataSource() {
        HikariDataSource hds = new HikariDataSource();
        hds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        hds.setJdbcUrl("jdbc:hsqldb:mem:QueryTest_testdb");
        hds.setUsername("sa");
        hds.setPassword("");
        dataSource = hds;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String objects_create_sql = IOUtils.toString(resolver.getResource("queries/hsql/objects_create.sql").getInputStream(), Charset.defaultCharset());
        String insert_sql = IOUtils.toString(resolver.getResource("queries/hsql/insert.sql").getInputStream(), Charset.defaultCharset());
        NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(dataSource);
        jt.execute(objects_create_sql, PreparedStatement::execute);
        people.add(map("IDX", 1, "FIRST_NAME", "John", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", dt("2019-08-19"), "RATING", 5));
        people.add(map("IDX", 2, "FIRST_NAME", "Jane", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", dt("2018-08-19"), "RATING", 6));
        people.add(map("IDX", 3, "FIRST_NAME", "Mary", "LAST_NAME", "Doherty", "SUBSCRIPTION_DATE", dt("2018-01-27"), "RATING", 4));
        people.add(map("IDX", 4, "FIRST_NAME", "Mark", "LAST_NAME", "Dundall", "SUBSCRIPTION_DATE", dt("2017-12-18"), "RATING", 6.5));
        jt.update(insert_sql, people.get(0));
        jt.update(insert_sql, people.get(1));
        jt.update(insert_sql, people.get(2));
        jt.update(insert_sql, people.get(3));
    }


    @BeforeEach
    public void prepareData() {
        int cnt = (int)(Math.random() * 30.0f);
        for(int i = 0; i < cnt; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            context.put(key, value);
        }
    }

    private void checkContextParameters(ArgumentCaptor<Map<String, Object>> captor) {
        Map<String, Object> capturedContext = captor.getValue();
        assertNotNull(capturedContext);
        for(Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            assertEquals(value, capturedContext.get(key));
        }
    }

    @Test
    @DisplayName("query: invokes namedParameterJdbcTemplate.query with correct arguments")
    @SuppressWarnings("unchecked")
    void testQueryCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        RowCallbackHandler rch = mock(RowCallbackHandler.class);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        query.query(rch);

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<RowCallbackHandler> arg3 = ArgumentCaptor.forClass(RowCallbackHandler.class);
        verify(namedParameterJdbcTemplate, times(1)).query(arg1.capture(), arg2.capture(), arg3.capture());
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(rch, arg3.getValue());
    }

    @Test
    @DisplayName("queryForList: invokes namedParameterJdbcTemplate.queryForList with correct arguments")
    @SuppressWarnings("unchecked")
    void testQueryForListCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        List<Map<String, Object>> expectedResults = new ArrayList<>();

        when(namedParameterJdbcTemplate.queryForList(eq(sql), anyMap())).thenReturn(expectedResults);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        List<?> results = query.queryForList();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).queryForList(arg1.capture(), arg2.capture());
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(expectedResults, results);
    }

    @Test
    @DisplayName("queryForInt: invokes namedParameterJdbcTemplate.queryForObject with correct arguments")
    @SuppressWarnings("unchecked")
    void testQueryForIntCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        Integer expectedResult = 10;
        when(namedParameterJdbcTemplate.queryForObject(eq(sql), anyMap(), eq(Integer.class))).thenReturn(expectedResult);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        Integer result = query.queryForInt();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(arg1.capture(), arg2.capture(), eq(Integer.class));
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("queryForLong: invokes namedParameterJdbcTemplate.queryForObject with correct arguments")
    @SuppressWarnings("unchecked")
    void testQueryForLongCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        Long expectedResult = 15L;
        when(namedParameterJdbcTemplate.queryForObject(eq(sql), anyMap(), eq(Long.class))).thenReturn(expectedResult);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        Long result = query.queryForLong();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).queryForObject(arg1.capture(), arg2.capture(), eq(Long.class));
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("queryForMap: invokes namedParameterJdbcTemplate.queryForMap with correct arguments")
    @SuppressWarnings("unchecked")
    void testQueryForMapCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        Map<String, Object> expectedResult = new HashMap<>();
        when(namedParameterJdbcTemplate.queryForMap(eq(sql), anyMap())).thenReturn(expectedResult);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        Map<String, Object> result = query.queryForMap();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).queryForMap(arg1.capture(), arg2.capture());
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("update: invokes namedParameterJdbcTemplate.update with correct arguments")
    @SuppressWarnings("unchecked")
    void testUpdateCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        Integer expectedResult = 10;
        when(namedParameterJdbcTemplate.update(eq(sql), anyMap())).thenReturn(expectedResult);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        Integer result = query.update();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).update(arg1.capture(), arg2.capture());
        assertEquals(sql, arg1.getValue());
        checkContextParameters(arg2);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("execute: invokes namedParameterJdbcTemplate.execute with correct arguments")
    @SuppressWarnings("unchecked")
    void testExecuteCallsJdbcTemplateWithCorrectArguments() {
        // given
        String sql = "select 1";
        when(namedParameterJdbcTemplate.execute(anyString(), anyMap(), any())).thenReturn(true);

        // when
        Query query = new Query(namedParameterJdbcTemplate, sql, context);
        boolean result = query.execute();

        // then
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> arg2 = ArgumentCaptor.forClass(Map.class);
        verify(namedParameterJdbcTemplate, times(1)).execute(arg1.capture(), arg2.capture(), any());
        assertEquals(sql, arg1.getValue());
        assertTrue(result);
        checkContextParameters(arg2);
    }

    @Test
    @DisplayName("iterate: iterates through all records with correct arguments")
    void testIterate() {
        // given
        String sql = "select * from \"cool_people\" where first_name like :name_like order by idx";
        Map<String, Object> params = new HashMap<>();
        params.put("name_like", "%a%");
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Query query = new Query(jdbcTemplate, sql, params);
        List<Map<String, Object>> expectation = jdbcTemplate.queryForList(sql, params);

        // when
        List<Map<String, Object>> results = new ArrayList<>();
        query.iterate(results::add);

        // then
        assertEquals(expectation.size(), results.size());

        for(int i = 0; i < results.size(); i++) {
            Map<String, Object> res = results.get(i);
            Map<String, Object> exp = expectation.get(i);
            assertEquals(exp.size(), res.size());
            for(Map.Entry<String, Object> kv : res.entrySet()) {
                assertEquals(kv.getValue(), exp.get(kv.getKey()));
            }
        }
    }
}
