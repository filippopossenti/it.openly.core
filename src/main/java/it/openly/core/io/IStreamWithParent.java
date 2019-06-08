package it.openly.core.io;

/**
 *
 * @param <T> The type
 *
 * @author filippo.possenti
 */
public interface IStreamWithParent<T> {
	T getParentStream();
}
