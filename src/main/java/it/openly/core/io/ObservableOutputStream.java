package it.openly.core.io;

import it.openly.core.patterns.observer.IObserver;
import it.openly.core.patterns.observer.StateInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A stream that allow observing the position of write operations on another
 * stream.
 * 
 * @author Filippo
 * 
 */
public class ObservableOutputStream extends OutputStream implements IObservableStream {

	private OutputStream destStream = null;
	private long position = 0;
	private List<IObserver> observers = new ArrayList<IObserver>();

	public ObservableOutputStream(OutputStream destStream) {
		super();
		this.destStream = destStream;
	}

	@Override
	public void close() throws IOException {
		destStream.close();
	}

	@Override
	public void flush() throws IOException {
		destStream.flush();
	}
	
	@Override
	public void write(int b) throws IOException {
		destStream.write(b);
		incrementProgress(1);
	}

	@Override
	public void write(byte[] b) throws IOException {
		destStream.write(b);
		incrementProgress(b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		destStream.write(b, off, len);
		incrementProgress(len);
	}

	private synchronized long incrementProgress(long incrementBy) {
		position += incrementBy;
		notifyObservers();
		return incrementBy;
	}

	@Override
	public synchronized long getPosition() {
		return position;
	}

	@Override
	public synchronized void attachObserver(IObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}

	}

	@Override
	public synchronized void detachObserver(IObserver observer) {
		observers.remove(observer);

	}

	@Override
	public synchronized void notifyObservers() {
		notifyObservers(new StateInfo(0, null, null));
	}

	@Override
	public synchronized void notifyObservers(StateInfo optionalState) {
		for (IObserver observer : observers) {
			observer.update(this, optionalState);
		}
	}

}
