package com.blockchain.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
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
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.blockchain.security.KeyPairs;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 *
 */
@Slf4j
@SuppressWarnings("unused")
public class CryptoHashUtils {

	private static final String END_ECDSA_PUBLIC_KEY    = "-----END ECDSA PUBLIC KEY-----";
	private static final String BEGIN_ECDSA_PUBLIC_KEY  = "-----BEGIN ECDSA PUBLIC KEY-----";
	private static final String END_ECDSA_PRIVATE_KEY   = "-----END ECDSA PRIVATE KEY-----";
	private static final String BEGIN_ECDSA_PRIVATE_KEY = "-----BEGIN ECDSA PRIVATE KEY-----";

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
	
	//////////////////////////////////// UTILITIES outside application context //////////////////////////////
	
	public static void saveKeyECSDAPairsInFile(String file) {
		KeyPair keyPairs = CryptoHashUtils.generateKeyPairs();
		
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

	public static KeyPairs loadKeyECSDAPairsInFile(String file) {
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
			
			return KeyPairs.generate(privateKey, publicKey);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	/**
	 * This where is calculated the Block's Merkle Root
	 * This Hash is used to prove the integrity of all of the transactions in this Block, that they weren't changed, tampered. 
	 * The value of this Merkle Root should always be the same if any of the original's values were not touched.
	 * 
	 * @author Ualter Junior
	 *
	 */
	public static class MerkleRoot {
		
		public static String calculateMerkleRoot(List<String> hashs) {
			String merkleRoot = null;
			if (hashs.size() == 1) {
				// Only one register, then there is nothing more to do, the only register's hash is also the Merkle Root
				merkleRoot =  CryptoHashUtils.applySHA256( hashs.get(0) );
			} else {
				while ( hashs.size() > 1 ) {
					hashs = calculateLeavesHash(hashs);
				}
				merkleRoot = hashs.get(0);
			}
			return merkleRoot;
		}

		private static List<String> calculateLeavesHash(List<String> hashs) {
			List<String> leavesHash = new ArrayList<String>();
			
			int index = 0;
			while ( true ) {
				
				String hash1 = hashs.get(index);
				
				index++;
				if ( index < hashs.size() ) {
					// Calculate the hash of the Pair
					String hash2    = hashs.get(index);
					String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash2 );
					leavesHash.add(hashLeaf);
				} else
				if ( index == hashs.size() ) {
					// The last one, without a pair, the total List is odd
					// As the Merkle Tree is a binary tree, we duplicate the last register to calculate the leaf
					String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash1 );
					leavesHash.add(hashLeaf);
				}
				
				index++;
				if ( index >= hashs.size() ) break;
			}
			return leavesHash;
		}
		
	}

}
