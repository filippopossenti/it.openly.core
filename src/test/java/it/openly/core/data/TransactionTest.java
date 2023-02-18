package it.openly.core.data;

import com.zaxxer.hikari.HikariDataSource;
import it.openly.core.exceptions.RollbackOnlyException;
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
import java.util.List;
import java.util.Map;

import static it.openly.core.test.TestUtils.dt;
import static it.openly.core.test.TestUtils.map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class TransactionTest {
    private static final List<Map<String, Object>> people = new ArrayList<>();
    private static DataSource dataSource;

    private QueryFactory queryFactory;

    @BeforeAll
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

    @BeforeEach
    public void setup() {
        queryFactory = new QueryFactory(dataSource);
    }


    @Test
    @DisplayName("rollback: rolls back the transaction after some changes have been made")
    void testRollback() {
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
        assertEquals(1, affectedRows);
        assertEquals(rating, ((Number)changedValue.get("RATING")).doubleValue());
        assertEquals(expectedRating, ((Number)actualValue.get("RATING")).doubleValue());
    }

    @Test
    @DisplayName("commit: commits the transation after changes have been made")
    void testCommit() {
        // given
        int idx = 3;
        double rating = 123.4;

        // when
        Transaction transaction = queryFactory.getTransaction();
        int affectedRows = queryFactory.update("update.sql", map("IDX", idx, "RATING", rating));
        transaction.commit();
        Map<String, Object> actualValue = queryFactory.queryForMap("get.sql", map("IDX", idx));

        // then
        assertEquals(1, affectedRows);
        assertEquals(rating, ((Number)actualValue.get("RATING")).doubleValue());
    }

    @Test
    @DisplayName("rollbackOnly: throws an exception when the commit method is called and the transaction is not committed")
    void testRollbackOnly() {
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
        assertEquals(1, affectedRows);
        assertEquals(rating, ((Number)changedValue.get("RATING")).doubleValue());
        assertEquals(expectedRating, ((Number)actualValue.get("RATING")).doubleValue());
    }

}
