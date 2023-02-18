package it.openly.core.data.support;

import it.openly.core.resources.IResourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultQueryResourceLoaderTest {

    @Mock
    DbProductDetector dbProductDetector;

    @Mock
    IResourceResolver resourceResolver;

    @Mock
    DataSource dataSource;

    @Test
    @DisplayName("Loading of database-specific queries")
    void testLoadDatabaseSpecificQuery() {
        // Given: there is a custom query for the specified database product
        String basepath = UUID.randomUUID().toString();
        String dbtype = UUID.randomUUID().toString();
        String queryname = UUID.randomUUID().toString();
        String expectedQueryText = UUID.randomUUID().toString();

        DefaultQueryResourceLoader defaultQueryResourceLoader = new DefaultQueryResourceLoader(resourceResolver, dbProductDetector, basepath);

        given(dbProductDetector.detectDbProduct(dataSource)).willReturn(dbtype);
        given(resourceResolver.hasResource(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname))).willReturn(true);
        given(resourceResolver.resolveStringResource(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname))).willReturn(expectedQueryText);

        // When: I try to load the custom query
        String actual = defaultQueryResourceLoader.loadQuery(dataSource, queryname);

        // Then: the database-specific query will be loaded correctly
        assertEquals(expectedQueryText, actual);
    }

    @Test
    @DisplayName("Loading of generic queries")
    void testLoadQueryDefaultResource() {
        // Given: there is no custom database query but there is the default one
        String basepath = UUID.randomUUID().toString();
        String dbtype = UUID.randomUUID().toString();
        String queryname = UUID.randomUUID().toString();
        String expectedQueryText = UUID.randomUUID().toString();

        DefaultQueryResourceLoader defaultQueryResourceLoader = new DefaultQueryResourceLoader(resourceResolver, dbProductDetector, basepath);

        given(dbProductDetector.detectDbProduct(dataSource)).willReturn(dbtype);
        given(resourceResolver.hasResource(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname))).willReturn(false);
        given(resourceResolver.resolveStringResource(FilenameUtils.concat(FilenameUtils.concat(basepath, "sql"), queryname))).willReturn(expectedQueryText);

        // When: I try to load the generic query
        String actual = defaultQueryResourceLoader.loadQuery(dataSource, queryname);

        // Then: the generic query will be loaded correctly
        assertEquals(expectedQueryText, actual);
    }

}
