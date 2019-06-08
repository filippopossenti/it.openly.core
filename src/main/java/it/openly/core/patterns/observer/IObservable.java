package it.openly.core.patterns.observer;

/**
 * @author filippo.possenti
 */
public interface IObservable {
	void attachObserver(IObserver observer);
	void detachObserver(IObserver observer);
	
	void notifyObservers();
	void notifyObservers(StateInfo optionalState);
}
