package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.security.PublicKey;

import com.blockchain.utils.CryptoHashUtils;

import lombok.Data;

@Data
public class TransactionOutput {
	
	private String hash;
	// New owner of these coins 
	private PublicKey recipient;
	// Amount of coins owned
	private BigDecimal value;
	// The HASH of the Transaction where this one originated (were created)
	private String parentTransactionHash;
	
	public TransactionOutput(PublicKey recipient, BigDecimal value, String parentTransactionHash) {
		this.recipient          = recipient;
		this.value               = value;
		this.parentTransactionHash = parentTransactionHash;
		
		String data              = CryptoHashUtils.encodeBase64(this.recipient) + this.value.toString() + this.parentTransactionHash;
		this.hash                = CryptoHashUtils.applySHA256(data);
	}
	
	/**
	 * These coins are mine?
	 * @param publicKey
	 * @return
	 */
	public boolean isMine(PublicKey publicKey) {
		return publicKey == this.recipient;
	}

	

}
