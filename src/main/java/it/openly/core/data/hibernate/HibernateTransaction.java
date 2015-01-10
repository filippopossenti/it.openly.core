package it.openly.core.data.hibernate;

import it.openly.core.data.ITransaction;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTransaction implements ITransaction {

	private Transaction transaction = null;
	private boolean rollbackOnly = false;

	public HibernateTransaction(Session session) {
		transaction = session.beginTransaction();
	}

	@Override
	public void commit() {
		if (!rollbackOnly)
			transaction.commit();
		else
			throw new RuntimeException("Transaction is marked as rollback-only.");
	}

	@Override
	public void rollback() {
		transaction.rollback();
	}

	@Override
	public void setRollbackOnly() {
		rollbackOnly = true;
	}

}
