package it.openly.core.patterns.observer;

public interface IObserver {
	public void update(IObservable target, StateInfo optionalState);
}
