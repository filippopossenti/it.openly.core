package it.openly.core.data.support;

import it.openly.core.data.support.DbProductDetector;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DbProductDetectorTest {

    @Parameterized.Parameters(name = "{index}: {0} = {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Oracle", "oracle" },
                { "Microsoft SQL Server", "mssql" },
                { "PostgreSQL", "postgresql" },
                { "MySQL", "mysql" },
                { "HSQL Database Engine", "hsql" },
                { "unknown database", "generic" }
        });
    }


    private DbProductDetector dbProductDetector = new DbProductDetector();

    private final String dbProductName;
    private final String expectedDbType;

    public DbProductDetectorTest(String dbProductName, String expectedDbType) {
        this.dbProductName = dbProductName;
        this.expectedDbType = expectedDbType;
    }

    @Test
    @SneakyThrows
    public void testDetectDbType() {
        // given
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getDatabaseProductName()).thenReturn(dbProductName);

        // when
        String dbType = dbProductDetector.detectDbProduct(ds);

        // then
        assertThat(dbType, equalTo(expectedDbType));
    }
}
