package it.openly.core.io;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * An output stream capable of calculating the digest of written data.
 *
 * @author filippo.possenti
 */
public class OutputStreamWithDigest extends ObservableOutputStream implements IStreamWithDigest {

	private final MessageDigest messageDigest;
	private byte[] digestValue = null;

	public OutputStreamWithDigest(OutputStream destStream) {
		this(destStream, null);
	}

	@SneakyThrows
	public OutputStreamWithDigest(OutputStream destStream, MessageDigest messageDigest) {
		super(destStream);
		this.messageDigest = messageDigest != null ? messageDigest : MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
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
	public byte[] getDigestValue() {
		return digestValue;
	}

	@Override
	public MessageDigest getMessageDigest() {
		return messageDigest;
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
