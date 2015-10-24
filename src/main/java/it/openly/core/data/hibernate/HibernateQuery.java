package it.openly.core.data.hibernate;

import it.openly.core.data.AbstractQuery;
import it.openly.core.data.IRowHandlerCallback;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

public class HibernateQuery extends AbstractQuery {

	private Session session;
	private boolean isNative = false;
	private Class<?> returnedType = null;

	public Session getSession() {
		return session;
	}

	public void setSession(Session value) {
		session = value;
	}

	public HibernateQuery(Session session, String sqlStatement, Map<String, ?> context, boolean isNative, Class<?> returnedType) {
		super(sqlStatement, context);
		this.session = session;
		this.isNative = isNative;
		this.returnedType = returnedType;
	}

	public Query createQuery(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		Query query = null;
		if (!isNative)
			query = session.createQuery(getSqlStatement());
		else
			query = session.createSQLQuery(getSqlStatement());
		if (returnedType != null)
			query.setResultTransformer(Transformers.aliasToBean(returnedType));
		Map<String, ?> context = mergeContext(contexts);
		for (String key : context.keySet()) {
			query.setParameter(key, context.get(key));
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	private Object getSingleColumn(Map<String, ?>... contexts) {
		Query query = createQuery(contexts);
		Map<String, ?> first = (Map<String, ?>) query.uniqueResult();
		Set<String> keys = first.keySet();
		if (keys.size() != 1)
			throw new RuntimeException("Only one column was expected. Actual number of columns: " + keys.size());
		for (String key : keys) {
			return first.get(key);
		}
		throw new RuntimeException("The impossible has just happened. Check for memory corruption situations...");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryForList(Map<String, ?>... contexts) {
		return createQuery(contexts).list();
	}

	@Override
	public int queryForInt(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return ((Integer) getSingleColumn(contexts)).intValue();
	}

	@Override
	public long queryForLong(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return ((Long) getSingleColumn(contexts)).longValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T queryForObject(Class<T> clazz, Map<String, ?>... contexts) {
		return (T) createQuery(contexts).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> queryForMap(Map<String, ?>... contexts) {
		return (Map<String, Object>)createQuery(contexts).uniqueResult();
	}

	@Override
	public int update(@SuppressWarnings("unchecked") Map<String, ?>... contexts) {
		return createQuery(contexts).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void iterate(IRowHandlerCallback<T> callback, Map<String, ?>... contexts) {
		long counter = 0;
		Query query = createQuery(contexts);
		ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
		while (results.next()) {
			T row = (T) results.get(0);
			callback.handleRow(row);
			counter++;
			if (counter % 100 == 0)
				getSession().flush();
		}
		getSession().flush();

		/*
		 * Iterator<T> it = (Iterator<T>)createQuery().iterate();
		 * while(it.hasNext()) { T row = it.next(); callback.handleRow(row); }
		 */
	}
}
