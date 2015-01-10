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

	public Query createQuery() {
		Query query = null;
		if (!isNative)
			query = session.createQuery(getSqlStatement());
		else
			query = session.createSQLQuery(getSqlStatement());
		if (returnedType != null)
			query.setResultTransformer(Transformers.aliasToBean(returnedType));
		for (String key : getContext().keySet()) {
			query.setParameter(key, getContext().get(key));
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	private Object getSingleColumn() {
		Query query = createQuery();
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
	public <T> List<T> queryForList() {
		return createQuery().list();
	}

	@Override
	public int queryForInt() {
		return ((Integer) getSingleColumn()).intValue();
	}

	@Override
	public long queryForLong() {
		return ((Long) getSingleColumn()).longValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T queryForObject(Class<T> clazz) {
		return (T) createQuery().uniqueResult();
	}

	@Override
	public int update() {
		return createQuery().executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void iterate(IRowHandlerCallback<T> callback) {
		long counter = 0;
		Query query = createQuery();
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
