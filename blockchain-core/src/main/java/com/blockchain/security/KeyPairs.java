package com.blockchain.security;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;

public class KeyPairs {
	
	@Autowired
	private Security security;
	
	static {
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	@Getter private PrivateKey privateKey;
	@Getter private PublicKey publicKey;

	public static KeyPairs generate() {
		return new KeyPairs();
	}

	private KeyPairs() {
		KeyPair keyPair = security.generateKeyPairs();
		this.privateKey = keyPair.getPrivate();
		this.publicKey  = keyPair.getPublic();
	}

	
	
}