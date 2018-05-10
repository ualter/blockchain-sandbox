package com.blockchain.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.RSAKeyGenParameterSpec;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SecurityRSA implements Security {

	@Override
	public KeyPair generateKeyPairs() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			RSAKeyGenParameterSpec rsaParamSpec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
			keyGen.initialize(rsaParamSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
			
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] sign(PrivateKey privateKey, String dataInput) {
		Signature rsa;
		byte[] signature = new byte[0];
		try {
			rsa = Signature.getInstance("RSA", "BC");
			rsa.initSign(privateKey);
			byte[] strByte = dataInput.getBytes();
			rsa.update(strByte);
			signature = rsa.sign();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		return signature;
	}

	@Override
	public boolean verifySignature(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature rsaVerify = Signature.getInstance("RSA", "BC");
			rsaVerify.initVerify(publicKey);
			rsaVerify.update(data.getBytes());
			return rsaVerify.verify(signature);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
