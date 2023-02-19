package it.openly.core.patterns.observer;

import java.util.List;

/**
 * @author filippo.possenti
 */
public interface IObservable {

	List<IObserver> getObservers();

	default void attachObserver(IObserver observer) {
		synchronized(getObservers()) {
			if (!getObservers().contains(observer)) {
				getObservers().add(observer);
			}
		}
	}

	default void detachObserver(IObserver observer) {
		synchronized(getObservers()) {
			getObservers().remove(observer);
		}
	}

	default void notifyObservers() {
		notifyObservers(new StateInfo(0, this, null));
	}

	default void notifyObservers(StateInfo optionalState) {
		synchronized(getObservers()) {
			for (IObserver observer : getObservers()) {
				observer.update(this, optionalState);
			}
		}
	}
}
