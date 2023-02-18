package it.openly.core.data.support;

import it.openly.core.data.QueryFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.sql.DataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DbProductDetectorTest {


    @ParameterizedTest
    @CsvSource(value = {"Oracle;oracle;",
            "Microsoft SQL Server;mssql;",
            "PostgreSQL;postgresql;",
            "MySQL;mysql;",
            "HSQL Database Engine;hsql;",
            "unknown database;generic;",
            "Custom Db 01;custom1;it/openly/core/data/custom-database-types.properties",
            "Custom Db 02;custom2;it/openly/core/data/custom-database-types.properties",
            "unknown database;generic;it/openly/core/data/custom-database-types.properties"},
            delimiter = ';')
    @SneakyThrows
    @DisplayName("Detection of database types")
    void testDetectDbType(String dbProductName, String expectedDbType, String customPropertiesFile) {
        // Given: a datasource configured to return a given product name
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getDatabaseProductName()).thenReturn(dbProductName);

        // Given: I'm using a custom properties file for the database product names
        DbProductDetector dbProductDetector;
        if(customPropertiesFile == null) {
            dbProductDetector= new DbProductDetector();
        }
        else {
            dbProductDetector = new DbProductDetector(loadCustomProperties(customPropertiesFile));
        }

        // When: I try to detect the database product name
        String dbType = dbProductDetector.detectDbProduct(ds);

        // Then: the detected database is correct
        assertEquals(expectedDbType, dbType);
    }

    @SneakyThrows
    private Properties loadCustomProperties(String customPropertiesFile) {
        Properties props = new Properties();
        try(InputStream fileData = QueryFactory.class.getClassLoader().getResourceAsStream(customPropertiesFile)) {
            props.load(fileData);
        }
        return props;
    }

}
