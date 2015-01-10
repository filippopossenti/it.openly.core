package it.openly.core.data.hibernate;

import it.openly.core.data.AbstractQueryFactory;
import it.openly.core.data.ContextUtils;
import it.openly.core.data.IQuery;
import it.openly.core.data.ITransaction;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HibernateQueryFactory extends AbstractQueryFactory {
	private Session session = null;
	private SessionFactory sessionFactory = null;

	public Session getSession() {
		return session;
	}

	public void setSession(Session value) {
		session = value;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory value) {
		sessionFactory = value;
	}

	@Override
	public ITransaction getTransaction() {
		return new HibernateTransaction(getSessionInternal());
	}

	@Override
	protected IQuery createQueryObject(String sql, Map<String, ?> context, ContextUtils ctx) {
		try {
			return new HibernateQuery(getSessionInternal(), sql, (ctx != null ? ctx.getUsedParametersMap() : context), ctx.getNative(), ((ctx.getReturnedType() != null) ? Class.forName(ctx.getReturnedType()) : null));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Hibernate queries use "||" as string concatenation operator. We make sure
	 * that our context will use this operator, as another one may be set in the
	 * global scope.
	 */
	@Override
	protected ContextUtils createPreprocessingContextUtils(Map<String, ?> context) {
		ContextUtils ctu = new ContextUtils(context);
		ctu.setStringConcatOperator("||");
		return ctu;
	}

	private Session getSessionInternal() {
		Session session = getSession();
		if (session == null) {
			session = sessionFactory.getCurrentSession();
		}
		return session;
	}
}
