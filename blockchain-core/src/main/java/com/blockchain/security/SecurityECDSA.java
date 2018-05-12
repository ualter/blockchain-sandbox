package com.blockchain.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Component;

import com.blockchain.security.SecurityEncryption.Algorithm;

import lombok.extern.slf4j.Slf4j;

@Component
@SecurityEncryption(Algorithm.ECDSA)
@Slf4j
public class SecurityECDSA implements Security {
	
	static {
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	private static final String END_ECDSA_PUBLIC_KEY    = "-----END ECDSA PUBLIC KEY-----";
	private static final String BEGIN_ECDSA_PUBLIC_KEY  = "-----BEGIN ECDSA PUBLIC KEY-----";
	private static final String END_ECDSA_PRIVATE_KEY   = "-----END ECDSA PRIVATE KEY-----";
	private static final String BEGIN_ECDSA_PRIVATE_KEY = "-----BEGIN ECDSA PRIVATE KEY-----";

	@Override
	public KeyPair generateKeyPairs() {
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

	@Override
	public byte[] sign(PrivateKey privateKey, String dataInput) {
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

	@Override
	public boolean verifySignature(PublicKey publicKey, String data, byte[] signature) {
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

	@Override
	public void saveKeyPairsToFile(String file) {
		KeyPair keyPairs = this.generateKeyPairs();
		
		String privateKeyEncoded = Base64.getEncoder().encodeToString(keyPairs.getPrivate().getEncoded());
		String publicKeyEncoded  = Base64.getEncoder().encodeToString(keyPairs.getPublic().getEncoded());
		
		List<String> lines = new ArrayList<String>();
		lines.add(BEGIN_ECDSA_PRIVATE_KEY);
		lines.add(privateKeyEncoded);
		lines.add(END_ECDSA_PRIVATE_KEY);
		lines.add(" ");
		lines.add(BEGIN_ECDSA_PUBLIC_KEY);
		lines.add(publicKeyEncoded);
		lines.add(END_ECDSA_PUBLIC_KEY);
		
		Path path = Paths.get(file);
		try {
			Files.write(path,lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public KeyPair loadKeyPairsFromFile(String file) {
		Path path = Paths.get(file);
		try {
			
			StringBuffer privateKeyEncoded = new StringBuffer();
			StringBuffer publicKeyEncoded  = new StringBuffer();
			
			boolean readPrivateKeyRegion  = false;
			boolean readPublicKeyRegion   = false;
			
			List<String> lines = Files.readAllLines(path);
			for(String line : lines) {
				if (BEGIN_ECDSA_PRIVATE_KEY.equals(line)) {
					readPrivateKeyRegion = true;
					continue;
				} else
				if (END_ECDSA_PRIVATE_KEY.equals(line)) {
					readPrivateKeyRegion = false;
				} else
				if (BEGIN_ECDSA_PUBLIC_KEY.equals(line)) {
					readPublicKeyRegion = true;
					continue;
				} else
				if (END_ECDSA_PUBLIC_KEY.equals(line)) {
					readPublicKeyRegion = false;
				}
				
				if (readPrivateKeyRegion) {
					privateKeyEncoded.append(line);
				}
				
				if (readPublicKeyRegion) {
					publicKeyEncoded.append(line);
				}
			}
			
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
			
			byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyEncoded.toString());
			byte[] publicKeyBytes  = Base64.getDecoder().decode(publicKeyEncoded.toString());
			
			// Get Back PrivateKey
			PKCS8EncodedKeySpec specPrivateKey = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(specPrivateKey);
			
			// get Back PublicKey
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
			
			return new KeyPair(publicKey, privateKey);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
	}

}
