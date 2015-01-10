package it.openly.core.patterns.observer;

public interface IObservable {
	public void attachObserver(IObserver observer);
	public void detachObserver(IObserver observer);
	
	public void notifyObservers();
	public void notifyObservers(StateInfo optionalState);
}
