package it.openly.core.patterns.observer;

/**
 * @author filippo.possenti
 */
@FunctionalInterface
public interface IObserver {
	void update(IObservable target, StateInfo optionalState);
}
