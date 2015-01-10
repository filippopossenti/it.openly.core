package it.openly.core.data.spring;

import it.openly.core.data.ITransaction;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransaction implements ITransaction {

	TransactionStatus txStatus = null;
	DataSourceTransactionManager transactionManager = null;
	boolean rollbackOnly = false;

	public SpringTransaction(DataSource dataSource) {
		transactionManager = new DataSourceTransactionManager(dataSource);
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		txStatus = transactionManager.getTransaction(transactionDefinition);

	}

	@Override
	public void commit() {
		if (!rollbackOnly) {
			try {
				transactionManager.commit(txStatus);
			} catch (TransactionException te) {
				throw new RuntimeException(te);
			}
		} else {
			throw new RuntimeException("Transaction is marked as rollback-only");
		}
	}

	@Override
	public void rollback() {
		try {
			transactionManager.rollback(txStatus);
		} catch (TransactionException te) {
			throw new RuntimeException(te);
		}
	}

	@Override
	public void setRollbackOnly() {
		rollbackOnly = true;
	}

}
