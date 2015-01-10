package it.openly.core.io;

import it.openly.core.patterns.observer.IObserver;
import it.openly.core.patterns.observer.StateInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A stream that allow observing the position of read operations on another
 * stream.
 * 
 * @author Filippo
 */
public class ObservableInputStream extends InputStream implements IObservableStream {

	private InputStream sourceStream = null;
	private long position = 0;
	private List<IObserver> observers = new ArrayList<IObserver>();

	public ObservableInputStream(InputStream sourceStream) {
		super();
		if (sourceStream == null) {
			throw new IllegalArgumentException("The sourceStream constructor argument cannot be null");
		}
		this.sourceStream = sourceStream;
	}

	@Override
	public void close() throws IOException {
		sourceStream.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		sourceStream.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return sourceStream.markSupported();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return (int) incrementProgress(sourceStream.read(b));
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return (int) incrementProgress(sourceStream.read(b, off, len));
	}

	@Override
	public int read() throws IOException {
		return (int) incrementProgress(sourceStream.read());
	}

	@Override
	public synchronized void reset() throws IOException {
		sourceStream.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return sourceStream.skip(n);
	}

	@Override
	public int available() throws IOException {
		return sourceStream.available();
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
