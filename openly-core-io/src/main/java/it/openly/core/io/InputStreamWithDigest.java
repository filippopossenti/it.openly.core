package it.openly.core.io;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * A stream capable of calculating a message digest over its data
 *
 * @author filippo.possenti
 */
public class InputStreamWithDigest extends ObservableInputStream implements IStreamWithDigest {

	private final MessageDigest messageDigest;

	/**
	 * Available only after the stream is closed, represents the value of the digest.
	 */
	@Getter
	private byte[] digestValue = null;

	public InputStreamWithDigest(InputStream sourceStream) {
		this(sourceStream, null);
	}

	@SneakyThrows
	public InputStreamWithDigest(InputStream sourceStream, MessageDigest messageDigest) {
		super(sourceStream);
		this.messageDigest = messageDigest != null ? messageDigest : MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
		Objects.requireNonNull(this.messageDigest, "messageDigest cannot be null. The argument was null and no default digest algorithm was found.");
	}
	
	@Override
	public MessageDigest getMessageDigest() {
		return messageDigest;
	}
	
	private void updateDigest(byte[] b, int off, int len) {
		synchronized(messageDigest) {
			messageDigest.update(b, off, len);
		}
	}
	
	private void calculateDigestValue() {
		synchronized(messageDigest) {
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
