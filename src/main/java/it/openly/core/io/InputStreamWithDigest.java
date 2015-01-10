package it.openly.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A stream capable of calculating a message digest over its data
 * @author Filippo
 *
 */
public class InputStreamWithDigest extends ObservableInputStream implements IStreamWithDigest {

	private MessageDigest messageDigest = null;
	private byte[] digestValue = null;
	
	public InputStreamWithDigest(InputStream sourceStream) {
		super(sourceStream);
		try {
			messageDigest = MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
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
	 * @return
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
