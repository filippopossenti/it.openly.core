package it.openly.core.io;

import java.security.MessageDigest;

public interface IStreamWithDigest {
	
	public final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
	
	public byte[] getDigestValue();
	
	public MessageDigest getMessageDigest();
}
