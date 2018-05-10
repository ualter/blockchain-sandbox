package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.security.PublicKey;

import com.blockchain.security.Security;

import lombok.Data;

@Data
public class TransactionOutput {
	
	private String hash;
	private PublicKey recipient;
	private BigDecimal value;
	private String parentTransactionHash;
	
	public TransactionOutput(PublicKey recipient, BigDecimal value, String parentTransactionHash) {
		this.recipient          = recipient;
		this.value               = value;
		this.parentTransactionHash = parentTransactionHash;
		
		String data              = Security.encodeBase64(this.recipient) + this.value.toString() + this.parentTransactionHash;
		this.hash                = Security.applySHA256(data);
	}
	
	/**
	 * These coins are mine?
	 */
	public boolean isMine(PublicKey publicKey) {
		return publicKey == this.recipient;
	}

	

}
