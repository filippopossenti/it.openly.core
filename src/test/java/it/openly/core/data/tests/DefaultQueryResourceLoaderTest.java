package it.openly.core.data.tests;

import it.openly.core.data.support.DbProductDetector;
import it.openly.core.data.support.DefaultQueryResourceLoader;
import it.openly.core.resources.IResourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultQueryResourceLoaderTest {

    @Mock
    DbProductDetector dbProductDetector;

    @Mock
    IResourceResolver resourceResolver;

    @Mock
    DataSource dataSource;

    @Test
    public void testLoadQuery() {
        // given
        String basepath = UUID.randomUUID().toString();
        String dbtype = UUID.randomUUID().toString();
        String queryname = UUID.randomUUID().toString();
        String expectedQueryText = UUID.randomUUID().toString();

        DefaultQueryResourceLoader defaultQueryResourceLoader = new DefaultQueryResourceLoader(resourceResolver, dbProductDetector, basepath, "");

        when(dbProductDetector.detectDbProduct(eq(dataSource))).thenReturn(dbtype);
        when(resourceResolver.hasResource(eq(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname)))).thenReturn(true);
        when(resourceResolver.resolveStringResource(eq(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname)))).thenReturn(expectedQueryText);

        // when
        String actual = defaultQueryResourceLoader.loadQuery(dataSource, queryname);

        // then
        assertThat(actual, equalTo(expectedQueryText));

    }

    @Test
    public void testLoadQueryDefaultResource() {
        // given
        String basepath = UUID.randomUUID().toString();
        String dbtype = UUID.randomUUID().toString();
        String queryname = UUID.randomUUID().toString();
        String expectedQueryText = UUID.randomUUID().toString();

        DefaultQueryResourceLoader defaultQueryResourceLoader = new DefaultQueryResourceLoader(resourceResolver, dbProductDetector, basepath, "");

        when(dbProductDetector.detectDbProduct(eq(dataSource))).thenReturn(dbtype);
        when(resourceResolver.hasResource(eq(FilenameUtils.concat(FilenameUtils.concat(basepath, dbtype), queryname)))).thenReturn(false);
        when(resourceResolver.resolveStringResource(eq(FilenameUtils.concat(FilenameUtils.concat(basepath, "sql"), queryname)))).thenReturn(expectedQueryText);

        // when
        String actual = defaultQueryResourceLoader.loadQuery(dataSource, queryname);

        // then
        assertThat(actual, equalTo(expectedQueryText));

    }

}
