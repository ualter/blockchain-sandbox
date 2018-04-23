package com.blockchain.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 *
 */
@Slf4j
@SuppressWarnings("unused")
public class CryptoHashUtils {

	public static String encodeBase64(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static boolean verifySignature(PublicKey publicKey, String data, byte[] signature) {
		return verifyECDSASig(publicKey, data, signature);
	}
	
	public static byte[] sign(PrivateKey privateKey, String dataInput) {
		return signWithECDSA(privateKey, dataInput);
	}
	
	public static KeyPair generateKeyPairs() {
		return generateECDSAKeyPairs();
	}
	
	private static KeyPair generateRSAKeyPairs() {
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
	private static KeyPair generateECDSAKeyPairs() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Verify Signature ECDSA Algorithm
	 * 
	 * @param publicKey
	 * @param data
	 * @param signature
	 * @return
	 */
	private static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Verify Signature RSA Algorithm
	 * 
	 * @param publicKey
	 * @param data
	 * @param signature
	 * @return
	 */
	private static boolean verifyRSASig(PublicKey publicKey, String data, byte[] signature) {
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

	/**
	 * Calculates the SHA-256 digest and returns the value as a hex string
	 * (commons-codec)
	 * 
	 * @param data
	 * @return
	 */
	public static String applySHA256(String data) {
		return DigestUtils.sha256Hex(data);
	}

	/**
	 * ECDSA Signature
	 * @param privateKey
	 * @param dataInput
	 * @return
	 */
	private static byte[] signWithECDSA(PrivateKey privateKey, String dataInput) {
		Signature dsa;
		byte[] signature = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = dataInput.getBytes();
			dsa.update(strByte);
			signature = dsa.sign();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		return signature;
	}
	
	/**
	 * RSA Signature
	 * @param privateKey
	 * @param dataInput
	 * @return
	 */
	private static byte[] signWithRSA(PrivateKey privateKey, String dataInput) {
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

}
