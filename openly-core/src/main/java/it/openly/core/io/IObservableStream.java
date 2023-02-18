package it.openly.core.io;

import it.openly.core.patterns.observer.IObservable;

/**
 * @author filippo.possenti
 */
public interface IObservableStream extends IObservable {
	long getPosition();
}
