package it.openly.core.data.spring;

import it.openly.core.data.ITransaction;

import javax.sql.DataSource;

import it.openly.core.exceptions.RollbackOnlyException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransaction implements ITransaction {

	private TransactionStatus txStatus;
	private DataSourceTransactionManager transactionManager;
	private boolean rollbackOnly = false;

	public SpringTransaction(DataSource dataSource) {
		transactionManager = new DataSourceTransactionManager(dataSource);
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		txStatus = transactionManager.getTransaction(transactionDefinition);
	}

	@Override
	public void commit() {
		if (!rollbackOnly) {
			transactionManager.commit(txStatus);
		} else {
			throw new RollbackOnlyException("Transaction is marked as rollback-only");
		}
	}

	@Override
	public void rollback() {
		transactionManager.rollback(txStatus);
	}

	@Override
	public void setRollbackOnly() {
		rollbackOnly = true;
	}

}
