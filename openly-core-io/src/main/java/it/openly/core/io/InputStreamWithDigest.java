package it.openly.core.io;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * A stream capable of calculating a message digest over its data
 *
 * @author filippo.possenti
 */
public class InputStreamWithDigest extends ObservableInputStream implements IStreamWithDigest {

	private final MessageDigest messageDigest;
	private byte[] digestValue = null;

	@SneakyThrows
	public InputStreamWithDigest(InputStream sourceStream) {
		super(sourceStream);
		messageDigest = MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
	}

	public InputStreamWithDigest(InputStream sourceStream, MessageDigest messageDigest) {
		super(sourceStream);
		this.messageDigest = messageDigest;
	}
	
	@Override
	public MessageDigest getMessageDigest() {
		return messageDigest;
	}
	
	/**
	 * Available only after the stream is closed, represents the value of the digest.
	 * @return The digest
	 */
	@Override
	public byte[] getDigestValue() {
		return digestValue;
	}
	
	private synchronized void updateDigest(byte[] b, int off, int len) {
		if(messageDigest != null) {
			messageDigest.update(b, off, len);
		}
	}
	
	private synchronized void calculateDigestValue() {
		if(messageDigest != null) {
			digestValue = messageDigest.digest();
		}
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int result = super.read(b);
		updateDigest(b, 0, result);
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = super.read(b, off, len);
		updateDigest(b, off, result);
		return result;
	}

	@Override
	public int read() throws IOException {
		int result = super.read();
		if(result >= 0) {
			updateDigest(new byte[] { (byte)result }, 0, 1);
		}
		return result;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		calculateDigestValue();
	}
	
}
