package it.openly.core.data.tests;

import com.zaxxer.hikari.HikariDataSource;
import it.openly.core.data.QueryFactory;
import it.openly.core.data.Transaction;
import it.openly.core.exceptions.RollbackOnlyException;
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
import java.util.List;
import java.util.Map;

import static it.openly.core.data.tests.TestUtils.dt;
import static it.openly.core.data.tests.TestUtils.map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class TransactionTest {
    private static List<Map<String, Object>> people = new ArrayList<>();
    private static DataSource dataSource;

    private QueryFactory queryFactory;

    @BeforeClass
    @SneakyThrows
    public static void prepareDataSource() {
        HikariDataSource hds = new HikariDataSource();
        hds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        hds.setJdbcUrl("jdbc:hsqldb:mem:TransactionTest_testdb");
        hds.setUsername("sa");
        hds.setPassword("");
        dataSource = hds;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String objects_create_sql = IOUtils.toString(resolver.getResource("queries/hsql/objects_create.sql").getInputStream());
        String insert_sql = IOUtils.toString(resolver.getResource("queries/hsql/insert.sql").getInputStream());
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

    @Before
    public void setup() {
        queryFactory = new QueryFactory(dataSource);
    }


    @Test
    public void testRollback() {
        // given
        int idx = 3;
        double rating = 123.4;
        double expectedRating = ((Number)queryFactory.queryForMap("get.sql", map("IDX", idx)).get("RATING")).doubleValue();

        // when
        Transaction transaction = queryFactory.getTransaction();
        int affectedRows = queryFactory.update("update.sql", map("IDX", idx, "RATING", rating));
        Map<String, Object> changedValue = queryFactory.queryForMap("get.sql", map("IDX", idx));
        transaction.rollback();
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(affectedRows, is(1));
        assertThat(((Number)changedValue.get("RATING")).doubleValue(), is(rating));
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(expectedRating));
    }

    @Test
    public void testCommit() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        Transaction transaction = queryFactory.getTransaction();
        int affectedRows = queryFactory.update("update.sql", map("IDX", idx, "RATING", rating));
        transaction.commit();
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(affectedRows, is(1));
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(rating));
    }

    @Test
    public void testRollbackOnly() {
        // given
        int idx = 3;
        double rating = 123.4;
        double expectedRating = ((Number)queryFactory.queryForMap("get.sql", map("IDX", idx)).get("RATING")).doubleValue();

        // when
        Transaction transaction = queryFactory.getTransaction();
        transaction.setRollbackOnly();
        int affectedRows = queryFactory.update("update.sql", map("IDX", idx, "RATING", rating));
        Map<String, Object> changedValue = queryFactory.queryForMap("get.sql", map("IDX", idx));
        try {
            transaction.commit();
            fail("RollbackOnlyException was not thrown.");
        }
        catch (RollbackOnlyException roe) {
            // empty on purpose
        }
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertThat(affectedRows, is(1));
        assertThat(((Number)changedValue.get("RATING")).doubleValue(), is(rating));
        assertThat(((Number)actualValue.get("RATING")).doubleValue(), is(expectedRating));
    }

}
