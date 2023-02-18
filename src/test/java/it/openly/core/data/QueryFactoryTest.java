package it.openly.core.data;

import com.zaxxer.hikari.HikariDataSource;
import it.openly.core.test.TestUtils;
import it.openly.core.test.pojos.CoolPerson;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class QueryFactoryTest {

    private static final List<Map<String, Object>> people = new ArrayList<>();
    private static DataSource dataSource;

    private QueryFactory queryFactory;

    @BeforeAll
    @SneakyThrows
    public static void prepareDataSource() {
        HikariDataSource hds = new HikariDataSource();
        hds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        hds.setJdbcUrl("jdbc:hsqldb:mem:QueryFactoryTest_testdb");
        hds.setUsername("sa");
        hds.setPassword("");
        dataSource = hds;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String objects_create_sql = IOUtils.toString(resolver.getResource("queries/hsql/objects_create.sql").getInputStream());
        String insert_sql = IOUtils.toString(resolver.getResource("queries/hsql/insert.sql").getInputStream());
        NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(dataSource);
        jt.execute(objects_create_sql, PreparedStatement::execute);
        people.add(TestUtils.map("IDX", 1, "FIRST_NAME", "John", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", TestUtils.dt("2019-08-19"), "RATING", 5.27));
        people.add(TestUtils.map("IDX", 2, "FIRST_NAME", "Jane", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", TestUtils.dt("2018-08-19"), "RATING", 6.15));
        people.add(TestUtils.map("IDX", 3, "FIRST_NAME", "Mary", "LAST_NAME", "Doherty", "SUBSCRIPTION_DATE", TestUtils.dt("2018-01-27"), "RATING", 4.83));
        people.add(TestUtils.map("IDX", 4, "FIRST_NAME", "Mark", "LAST_NAME", "Dundall", "SUBSCRIPTION_DATE", TestUtils.dt("2017-12-18"), "RATING", 6.54));
        jt.update(insert_sql, people.get(0));
        jt.update(insert_sql, people.get(1));
        jt.update(insert_sql, people.get(2));
        jt.update(insert_sql, people.get(3));
    }

    @BeforeEach
    public void setup() {
        queryFactory = new QueryFactory(dataSource);
    }

    @Test
    @DisplayName("queryForList: applies correct filter and returns correct results")
    void testQueryForList() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<Map<String, Object>> actualResults = queryFactory.queryForList("list.sql", TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults.size(), actualResults.size());
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            Map<String, Object> actual = actualResults.get(i);
            assertEquals(expectation.get("IDX"), actual.get("IDX"));
        }
    }

    @Test
    @DisplayName("queryForBeans: applies correct filter and returns beans of the desired type")
    void testQueryForBeans() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<CoolPerson> actualResults = queryFactory.queryForBeans("list.sql", CoolPerson.class, TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults.size(), actualResults.size());
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            CoolPerson actual = actualResults.get(i);
            assertEquals(expectation.get("IDX"), actual.getIdx());
            assertEquals(expectation.get("FIRST_NAME"), actual.getFirstName());
            assertEquals(expectation.get("LAST_NAME"), actual.getLastName());
            assertEquals(((Date)expectation.get("SUBSCRIPTION_DATE")).getTime(), actual.getSubscriptionDate().getTime());
            assertEquals(((Number)expectation.get("RATING")).doubleValue(), actual.getRating().doubleValue());
        }
    }

    @Test
    @DisplayName("queryForInt: applies correct filter and returns the correct value")
    void testQueryForInt() {
        // given
        String lastName = "Doe";
        long expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).count();

        // when
        long actualResults = queryFactory.queryForInt("count.sql", TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults, actualResults);
    }

    @Test
    @DisplayName("queryForLong: applies correct filter and returns the correct value")
    void testQueryForLong() {
        // given
        String lastName = "Doe";
        long expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).count();

        // when
        long actualResults = queryFactory.queryForLong("count.sql", TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults, actualResults);
    }

    @Test
    @DisplayName("queryForMap: applies correct filter and returns a Map with correct values")
    void testQueryForMap() {
        // given
        int idx = 3;
        Map<String, Object> expectedPerson = people.stream().filter(m -> m.get("IDX").equals(idx)).findAny().orElseThrow(IllegalArgumentException::new);

        // when
        Map<String, Object> actualPerson = queryFactory.queryForMap("get.sql", TestUtils.map("IDX", idx));

        // then
        assertEquals(expectedPerson.get("FIRST_NAME"), actualPerson.get("FIRST_NAME"));

    }

    @Test
    @DisplayName("queryForBean: applies correct filter and returns a bean of the desired type with the correct values")
    void testQueryForBean() {
        // given
        int idx = 3;
        Map<String, Object> expectedPerson = queryFactory.queryForMap("get.sql", TestUtils.map("IDX", idx));

        // when
       CoolPerson actualPerson = queryFactory.queryForBean("get.sql", CoolPerson.class, TestUtils.map("IDX", idx));

        // then
        assertEquals(expectedPerson.get("IDX"), actualPerson.getIdx());
        assertEquals(expectedPerson.get("FIRST_NAME"), actualPerson.getFirstName());
        assertEquals(expectedPerson.get("LAST_NAME"), actualPerson.getLastName());
        assertEquals(((Date)expectedPerson.get("SUBSCRIPTION_DATE")).getTime(), actualPerson.getSubscriptionDate().getTime());
        assertEquals(((Number)expectedPerson.get("RATING")).doubleValue(), actualPerson.getRating().doubleValue());
    }

    @Test
    @DisplayName("queryForObject: applies correct filter and returns an object of the specified type")
    void testQueryForObject() {
        // given
        int idx = 3;
        String expectedPersonName = (String)people.stream().filter(m -> m.get("IDX").equals(idx)).findAny().orElseThrow(IllegalArgumentException::new).get("FIRST_NAME");

        // when
       String actualPersonName = queryFactory.queryForObject("getfield.sql", String.class, TestUtils.map("IDX", idx));

        // then
        assertEquals(expectedPersonName, actualPersonName);
    }

    @Test
    @DisplayName("update: applies correct arguments and updates the correct rows")
    void testUpdate() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        int affectedRows = queryFactory.update("update.sql", TestUtils.map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", TestUtils.map("IDX", idx));


        // then
        assertEquals(1, affectedRows);
        assertEquals(rating, ((Number)actualValue.get("RATING")).doubleValue());
    }

    @Test
    @DisplayName("iterate: applies correct arguments and iterates through the correct results with correct values")
    void testIterate() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        LongAdder adder = new LongAdder();
        queryFactory.iterate("list.sql", r-> adder.increment(), TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults.size(), adder.intValue());
    }

    @Test
    @DisplayName("iterateBeans: applies correct arguments and iterates through beans of the correct type with correct values")
    void testIterateBeans() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<CoolPerson> actualResults = new ArrayList<>();
        queryFactory.iterate("list.sql", CoolPerson.class, actualResults::add, TestUtils.map("LAST_NAME", lastName));

        // then
        assertEquals(expectedResults.size(), actualResults.size());
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            CoolPerson actual = actualResults.get(i);
            assertEquals(expectation.get("IDX"), actual.getIdx());
            assertEquals(expectation.get("FIRST_NAME"), actual.getFirstName());
            assertEquals(expectation.get("LAST_NAME"), actual.getLastName());
            assertEquals(((Date)expectation.get("SUBSCRIPTION_DATE")).getTime(), actual.getSubscriptionDate().getTime());
            assertEquals(((Number)expectation.get("RATING")).doubleValue(), actual.getRating().doubleValue());
        }

    }

    @Test
    @DisplayName("execute: applies correct arguments and runs the specified script correctly")
    void testExecute() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        queryFactory.execute("update.sql", TestUtils.map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", TestUtils.map("IDX", idx));

        // then
        assertEquals(rating, ((Number)actualValue.get("RATING")).doubleValue());
    }

    @Test
    @DisplayName("execute: invokes the specified callback")
    void testExecutePreparedStatementCallback() {
        // given
        int idx = 3;
        double rating = 456.7;
        LongAdder calls = new LongAdder();

        // when
        queryFactory.execute("update.sql", ps -> {
            calls.increment();
            return ps.execute();
        }, TestUtils.map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", TestUtils.map("IDX", idx));

        // then
        assertEquals(1, calls.intValue());
        assertEquals(rating, ((Number)actualValue.get("RATING")).doubleValue());
    }

}
