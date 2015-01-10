package it.openly.core.data;

public interface ITransaction {
	void commit();
	void rollback();
	void setRollbackOnly();
}
