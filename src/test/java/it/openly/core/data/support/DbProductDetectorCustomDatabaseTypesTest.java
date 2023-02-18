package it.openly.core.data.support;

import it.openly.core.data.support.DbProductDetector;
import it.openly.core.data.QueryFactory;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DbProductDetectorCustomDatabaseTypesTest {

    @Parameterized.Parameters(name = "{index}: {0} = {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Custom Db 01", "custom1" },
                { "Custom Db 02", "custom2" },
                { "unknown database", "generic" }
        });
    }


    private final String dbProductName;
    private final String expectedDbType;

    public DbProductDetectorCustomDatabaseTypesTest(String dbProductName, String expectedDbType) {
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

        DbProductDetector dbProductDetector = new DbProductDetector(loadCustomProperties());

        // when
        String dbType = dbProductDetector.detectDbProduct(ds);

        // then
        assertThat(dbType, equalTo(expectedDbType));
    }

    @SneakyThrows
    private Properties loadCustomProperties() {
        Properties props = new Properties();
        try(InputStream fileData = QueryFactory.class.getClassLoader().getResourceAsStream("it/openly/core/data/custom-database-types.properties")) {
            props.load(fileData);
        }
        return props;
    }

}
