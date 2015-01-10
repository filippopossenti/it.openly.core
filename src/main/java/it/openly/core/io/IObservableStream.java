package it.openly.core.io;

import it.openly.core.patterns.observer.IObservable;

public interface IObservableStream extends IObservable {
	long getPosition();
}
