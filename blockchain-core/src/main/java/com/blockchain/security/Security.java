package com.blockchain.security;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

public interface Security {
	
	public KeyPair generateKeyPairs();
	public byte[] sign(PrivateKey privateKey, String dataInput);
	public boolean verifySignature(PublicKey publicKey, String data, byte[] signature);
	public void saveKeyPairsToFile(String file);
	public KeyPair loadKeyPairsFromFile(String file);
	
	public static String encodeBase64(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static String applySHA256(String data) {
		return DigestUtils.sha256Hex(data);
	}
	
}
