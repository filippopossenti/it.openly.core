package it.openly.core.data;

import javax.sql.DataSource;

/**
 * @author filippo.possenti
 */
public interface IQueryResourceLoader {
    String loadQuery(DataSource dataSource, String namedQuery);
}
