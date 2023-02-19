package it.openly.core.io;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * An output stream capable of calculating the digest of written data.
 *
 * @author filippo.possenti
 */
public class OutputStreamWithDigest extends ObservableOutputStream implements IStreamWithDigest {

	@Getter
	private final MessageDigest messageDigest;

	/**
	 * Available only after the stream is closed, represents the value of the digest.
	 */
	@Getter
	private byte[] digestValue = null;

	public OutputStreamWithDigest(OutputStream destStream) {
		this(destStream, null);
	}

	@SneakyThrows
	public OutputStreamWithDigest(OutputStream destStream, MessageDigest messageDigest) {
		super(destStream);
		this.messageDigest = messageDigest != null ? messageDigest : MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
		Objects.requireNonNull(this.messageDigest, "messageDigest cannot be null. The argument was null and no default digest algorithm was found.");
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
	public void write(int b) throws IOException {
		super.write(b);
		updateDigest(new byte[] { (byte)b }, 0, 1);
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		updateDigest(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		updateDigest(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		calculateDigestValue();
	}
	

}
