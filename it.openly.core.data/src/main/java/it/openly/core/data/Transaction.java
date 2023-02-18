package it.openly.core.data;

import it.openly.core.exceptions.RollbackOnlyException;
import lombok.NonNull;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

/**
 * Convenience class to manually manage transactions.<br/>
 * Note that transactions dealt with this class may cause problems if transactionality is already
 * being managed by Spring's own facilities.
 *
 * @author filippo.possenti
 */
public class Transaction {

	private TransactionStatus txStatus;
	private DataSourceTransactionManager transactionManager;
	private boolean rollbackOnly = false;

	public Transaction(@NonNull DataSource dataSource) {
		transactionManager = new DataSourceTransactionManager(dataSource);
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		txStatus = transactionManager.getTransaction(transactionDefinition);
	}

	/**
	 * Commits a transaction.
	 */
	public void commit() {
		if (!rollbackOnly) {
			transactionManager.commit(txStatus);
		} else {
			throw new RollbackOnlyException("Transaction is marked as rollback-only");
		}
	}

	/**
	 * Rolls back a transaction
	 */
	public void rollback() {
		transactionManager.rollback(txStatus);
	}

	/**
	 * Marks a transaction as rollback-only.<br/>
	 * When calling the {@link #rollback() rollback} method everything will work correctly in this case, whereas calling the
	 * {@link #commit() commit} method will result in a {@link it.openly.core.exceptions.RollbackOnlyException RollbackOnlyException} exception being thrown.
	 */
	public void setRollbackOnly() {
		rollbackOnly = true;
	}

}
