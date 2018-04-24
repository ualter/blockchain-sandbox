package com.blockchain.utils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import lombok.Getter;

/**
 * 
 * @author Ualter Junior
 *
 */
public class KeyPairs {
	
	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	@Getter private PrivateKey privateKey;
	@Getter private PublicKey publicKey;

	public static KeyPairs generate() {
		return new KeyPairs(true);
	}
	
	public static KeyPairs generate(PrivateKey privateKey, PublicKey publicKey) {
		KeyPairs keyPairs   = new KeyPairs(false);
		keyPairs.privateKey = privateKey;
		keyPairs.publicKey  = publicKey;
		return keyPairs;
	}

	private KeyPairs(boolean generateKeys) {
		if (generateKeys) {
			KeyPair keyPair = CryptoHashUtils.generateKeyPairs();
			this.privateKey = keyPair.getPrivate();
			this.publicKey  = keyPair.getPublic();
		}
	}

	
	
}