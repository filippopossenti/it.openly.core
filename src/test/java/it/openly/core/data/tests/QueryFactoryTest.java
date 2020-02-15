package it.openly.core.data.tests;

import com.zaxxer.hikari.HikariDataSource;
import it.openly.core.data.QueryFactory;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
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

import static it.openly.core.data.tests.TestUtils.dt;
import static it.openly.core.data.tests.TestUtils.map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class QueryFactoryTest {

    private static List<Map<String, Object>> people = new ArrayList<>();
    private static DataSource dataSource;

    private QueryFactory queryFactory;

    @BeforeClass
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
        people.add(map("IDX", 1, "FIRST_NAME", "John", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", dt("2019-08-19"), "RATING", 5.27));
        people.add(map("IDX", 2, "FIRST_NAME", "Jane", "LAST_NAME", "Doe", "SUBSCRIPTION_DATE", dt("2018-08-19"), "RATING", 6.15));
        people.add(map("IDX", 3, "FIRST_NAME", "Mary", "LAST_NAME", "Doherty", "SUBSCRIPTION_DATE", dt("2018-01-27"), "RATING", 4.83));
        people.add(map("IDX", 4, "FIRST_NAME", "Mark", "LAST_NAME", "Dundall", "SUBSCRIPTION_DATE", dt("2017-12-18"), "RATING", 6.54));
        jt.update(insert_sql, people.get(0));
        jt.update(insert_sql, people.get(1));
        jt.update(insert_sql, people.get(2));
        jt.update(insert_sql, people.get(3));
    }

    @Before
    public void setup() {
        queryFactory = new QueryFactory(dataSource);
    }

    @Test
    public void testQueryForList() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<Map<String, Object>> actualResults = queryFactory.queryForList("list.sql", map("LAST_NAME", lastName));

        // then
        assertThat(actualResults.size(), is(expectedResults.size()));
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            Map<String, Object> actual = actualResults.get(i);
            assertThat(actual.get("IDX"), is(expectation.get("IDX")));
        }
    }

    @Test
    public void testQueryForBeans() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<CoolPerson> actualResults = queryFactory.queryForBeans("list.sql", CoolPerson.class, map("LAST_NAME", lastName));

        // then
        assertThat(actualResults.size(), is(expectedResults.size()));
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            CoolPerson actual = actualResults.get(i);
            assertThat(actual.getIdx(), is(expectation.get("IDX")));
            assertThat(actual.getFirstName(), is(expectation.get("FIRST_NAME")));
            assertThat(actual.getLastName(), is(expectation.get("LAST_NAME")));
            assertThat(actual.getSubscriptionDate().getTime(), is(((Date)expectation.get("SUBSCRIPTION_DATE")).getTime()));
            assertThat(actual.getRating().doubleValue(), is(((Number)expectation.get("RATING")).doubleValue()));
        }
    }

    @Test
    public void testQueryForInt() {
        // given
        String lastName = "Doe";
        long expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).count();

        // when
        long actualResults = (long)queryFactory.queryForInt("count.sql", map("LAST_NAME", lastName));

        // then
        assertThat(actualResults, is(expectedResults));
    }

    @Test
    public void testQueryForLong() {
        // given
        String lastName = "Doe";
        long expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).count();

        // when
        long actualResults = queryFactory.queryForLong("count.sql", map("LAST_NAME", lastName));

        // then
        assertThat(actualResults, is(expectedResults));
    }

    @Test
    public void testQueryForMap() {
        // given
        int idx = 3;
        Map<String, Object> expectedPerson = people.stream().filter(m -> m.get("IDX").equals(idx)).findAny().orElseThrow(IllegalArgumentException::new);

        // when
        Map<String, Object> actualPerson = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(actualPerson.get("FIRST_NAME"), is(expectedPerson.get("FIRST_NAME")));

    }

    @Test
    public void testQueryForBean() {
        // given
        int idx = 3;
        Map<String, Object> expectedPerson = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // when
       CoolPerson actualPerson = queryFactory.queryForBean("get.sql", CoolPerson.class, map("IDX", idx));

        // then
        assertThat(actualPerson.getIdx(), is(expectedPerson.get("IDX")));
        assertThat(actualPerson.getFirstName(), is(expectedPerson.get("FIRST_NAME")));
        assertThat(actualPerson.getLastName(), is(expectedPerson.get("LAST_NAME")));
        assertThat(actualPerson.getSubscriptionDate().getTime(), is(((Date)expectedPerson.get("SUBSCRIPTION_DATE")).getTime()));
        assertThat(actualPerson.getRating().doubleValue(), is(((Number)expectedPerson.get("RATING")).doubleValue()));
    }

    @Test
    public void testQueryForObject() {
        // given
        int idx = 3;
        String expectedPersonName = (String)people.stream().filter(m -> m.get("IDX").equals(idx)).findAny().orElseThrow(IllegalArgumentException::new).get("FIRST_NAME");

        // when
       String actualPersonName = queryFactory.queryForObject("getfield.sql", String.class, map("IDX", idx));

        // then
        assertThat(actualPersonName, is(expectedPersonName));
    }

    @Test
    public void testUpdate() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        int affectedRows = queryFactory.update("update.sql", map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));


        // then
        assertThat(affectedRows, is(1));
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(rating));
    }

    @Test
    public void testIterate() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        LongAdder adder = new LongAdder();
        queryFactory.iterate("list.sql", r-> adder.increment(), map("LAST_NAME", lastName));

        // then
        assertThat(adder.intValue(), is(expectedResults.size()));
    }

    @Test
    public void testIterateBeans() {
        // given
        String lastName = "Doe";
        List<Map<String, Object>> expectedResults = people.stream().filter(m -> m.get("LAST_NAME").equals(lastName)).collect(Collectors.toList());

        // when
        List<CoolPerson> actualResults = new ArrayList<>();
        queryFactory.iterate("list.sql", CoolPerson.class, actualResults::add, map("LAST_NAME", lastName));

        // then
        assertThat(actualResults.size(), is(expectedResults.size()));
        for(int i = 0; i < expectedResults.size(); i++) {
            Map<String, Object> expectation = expectedResults.get(i);
            CoolPerson actual = actualResults.get(i);
            assertThat(actual.getIdx(), is(expectation.get("IDX")));
            assertThat(actual.getFirstName(), is(expectation.get("FIRST_NAME")));
            assertThat(actual.getLastName(), is(expectation.get("LAST_NAME")));
            assertThat(actual.getSubscriptionDate().getTime(), is(((Date)expectation.get("SUBSCRIPTION_DATE")).getTime()));
            assertThat(actual.getRating().doubleValue(), is(((Number)expectation.get("RATING")).doubleValue()));
        }

    }

    @Test
    public void testExecute() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        queryFactory.execute("update.sql", map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(rating));
    }

    @Test
    public void testExecutePreparedStatementCallback() {
        // given
        int idx = 3;
        double rating = 456.7;

        // when
        queryFactory.execute("update.sql", PreparedStatement::execute, map("IDX", idx, "RATING", rating));
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(rating));
    }

}
