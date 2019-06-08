package it.openly.core.data;

/**
 * 
 * @author filippo.possenti
 */
@FunctionalInterface
public interface IRowHandlerCallback<T> {
	void handleRow(T row);
}
