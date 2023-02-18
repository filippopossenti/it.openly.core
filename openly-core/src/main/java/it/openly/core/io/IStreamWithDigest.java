package it.openly.core.io;

import java.security.MessageDigest;

/**
 * @author filippo.possenti
 */
public interface IStreamWithDigest {
	
	final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
	
	byte[] getDigestValue();
	
	MessageDigest getMessageDigest();
}
