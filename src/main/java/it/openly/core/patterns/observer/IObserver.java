package it.openly.core.patterns.observer;

public interface IObserver {
	void update(IObservable target, StateInfo optionalState);
}
