package it.openly.core.data;

import it.openly.core.data.support.DefaultQueryResourceLoader;
import it.openly.core.data.support.SimpleQueryTemplateProcessor;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main entry point object for this package. This class allows to easily execute native queries
 * based off a template, typically stored as a separate .sql file somewhere on the classpath.<br/>
 * By default, an instance of this class will process queries living in classpath:/queries/[dbproduct]/**.sql
 * using the embedded {@link it.openly.core.data.support.SimpleQueryTemplateProcessor SimpleQueryTemplateProcessor} class
 * to alter the queries based on the provided contexts.
 *
 * @author filippo.possenti
 */
public class QueryFactory {
    private ITemplateProcessor templateProcessor;
    private DataSource dataSource;
    private IQueryResourceLoader queryResourceLoader;

    public QueryFactory(DataSource dataSource, ITemplateProcessor templateProcessor, IQueryResourceLoader queryResourceLoader) {
        this.templateProcessor = templateProcessor;
        this.dataSource = dataSource;
        this.queryResourceLoader = queryResourceLoader;
    }

    public QueryFactory(DataSource dataSource) {
        this(dataSource, new SimpleQueryTemplateProcessor(true), new DefaultQueryResourceLoader());
    }

    /**
     * Configures and returns a Transaction object that can be used to executes subsequent queries transactionally.<br/>
     * Note that the use of this facility should be used with Spring's embedded transaction management facilities in
     * a mutually exclusive way.
     * @return A {@link Transaction Transaction} object, linked to the current datasource.
     */
    public Transaction getTransaction() {
        return new Transaction(dataSource);
    }

    /**
     * Creates a query object based on the specified named query and context variables.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return A {@link it.openly.core.data.Query Query} object
     */
    @SafeVarargs
    public final Query createQuery(String namedQuery, Map<String, Object>... contexts) {
        String sqlStatementTemplate = queryResourceLoader.loadQuery(dataSource, namedQuery);
        return createQueryFromTemplate(sqlStatementTemplate, contexts);
    }

    /**
     * Creates a query object based on the specified SQL and context variables.
     * @param sqlStatementTemplate The query SQL.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return A {@link it.openly.core.data.Query Query} object
     */
    @SafeVarargs
    public final Query createQueryFromTemplate(String sqlStatementTemplate, Map<String, Object>... contexts) {
        Map<String, Object> context = Stream.of(contexts).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ProcessedTemplate processedTemplate = templateProcessor.processTemplate(sqlStatementTemplate, context);
        return new Query(new NamedParameterJdbcTemplate(dataSource), processedTemplate.getSql(), processedTemplate.getContext());
    }

    /**
     * Executes the query expecting a list of {@link java.util.Map Map<String, Object>} as a result
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return A list of {@link java.util.Map Map<String, Object>} objects.
     */
    @SafeVarargs
    public final List<Map<String, Object>> queryForList(String namedQuery, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForList();
    }

    /**
     * Executes a query and returns a list of beans of the specified type.<br/>
     * The columns are converted according to spring's {@link org.springframework.jdbc.core.BeanPropertyRowMapper BeanPropertyRowMapper} rules.<br/>
     * It's important to remember that this framework is not meant to handle relationships, meaning that the bean is expected
     * to contain only primitive (eventually boxed) types.<br/>
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param clazz The bean's class.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @param <T> The type.
     * @return A list of beans.
     */
    @SafeVarargs
    public final <T> List<T> queryForBeans(String namedQuery, Class<T> clazz, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForBeans(clazz);
    }

    /**
     * Executes a query and returns an integer
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return An integer
     */
    @SafeVarargs
    public final int queryForInt(String namedQuery, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForInt();
    }

    /**
     * Executes a query and returns a long
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return A long
     */
    @SafeVarargs
    public final long queryForLong(String namedQuery, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForLong();
    }

    /**
     * Executes a query and returns an object. Note that in this case the object is expected to be
     * a primitive (possibly boxed) java type. Beans are not allowed.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param clazz The object type
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @param <T> The type.
     * @return The object
     */
    @SafeVarargs
    public final <T> T queryForObject(String namedQuery, Class<T> clazz, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForObject(clazz);
    }

    /**
     * Executes a query and returns a {@link java.util.Map Map<String, Object>}.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return A {@link java.util.Map Map<String, Object>} object
     */
    @SafeVarargs
    public final Map<String, Object> queryForMap(String namedQuery, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForMap();
    }

    /**
     * Executes a query and returns a bean of the specified type.<br/>
     * The columns are converted according to spring's {@link org.springframework.jdbc.core.BeanPropertyRowMapper BeanPropertyRowMapper} rules.<br/>
     * It's important to remember that this framework is not meant to handle relationships, meaning that the bean is expected
     * to contain only primitive (eventually boxed) types.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param clazz The bean's class.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @param <T> The type.
     * @return A bean.
     */
    @SafeVarargs
    public final <T> T queryForBean(String namedQuery, Class<T> clazz, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).queryForBean(clazz);
    }

    /**
     * Executes an update query and returns the number of affected rows.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @return The number of affected rows.
     */
    @SafeVarargs
    public final int update(String namedQuery, Map<String, Object>... contexts) {
        return createQuery(namedQuery, contexts).update();
    }

    /**
     * Executes a query and iterates through the ResultSet executing the specified callback for
     * each row. This is useful for large datasets to avoid keeping data in memory while it's
     * being processed.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param callback The callback
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     */
    @SafeVarargs
    public final void iterate(String namedQuery, IRowHandlerCallback<Map<String, Object>> callback, Map<String, Object>... contexts) {
        createQuery(namedQuery, contexts).iterate(callback);
    }

    /**
     * Executes a query and iterates through the ResultSet executing the specified callback for
     * each row. This is useful for large datasets to avoid keeping data in memory while it's
     * being processed.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param clazz The class of the beans for processing
     * @param callback The callback
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @param <T> The type.
     */
    @SafeVarargs
    public final <T> void iterate(String namedQuery, Class<T> clazz, final IRowHandlerCallback<T> callback, Map<String, Object>... contexts) {
        createQuery(namedQuery, contexts).iterate(clazz, callback);
    }

    /**
     * Executes a statement.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     */
    @SafeVarargs
    public final void execute(String namedQuery, Map<String, Object>... contexts) {
        createQuery(namedQuery, contexts).execute();
    }

    /**
     * Executes a statement using the provided callback to drive execution. Don't forget to call the
     * {@link PreparedStatement#execute() PreparedStatement.execute} method.
     * @param namedQuery The named query name. The name is defined as a relative path from the classpath:/queries/[dbproduct] path, without the ./sql extension. For example: "hello/world" will try to load the classpath:/queries/[dbproduct]/hello/world.sql file.
     * @param preparedStatementCallback The callback
     * @param contexts A variable number of {@link java.util.Map Map} objects containing the variables driving the query. Having multiple Map objects allows to more easily separate parameters, for example filter parameters could be in one Map and paging parameters in another Map.
     * @param <T> The type.
     */
    @SafeVarargs
    public final <T> void execute(String namedQuery, PreparedStatementCallback<T> preparedStatementCallback, Map<String, Object>... contexts) {
        createQuery(namedQuery, contexts).execute(preparedStatementCallback);
    }

}
