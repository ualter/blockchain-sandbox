package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.security.PublicKey;

import org.apache.commons.lang.StringUtils;

import com.blockchain.security.Security;

import lombok.Getter;

public class TransactionOutput {
	
	@Getter private String hash;
	@Getter private Wallet recipient;
	@Getter private BigDecimal value;
	@Getter private String parentTransactionHash;
	
	public TransactionOutput(Wallet recipient, BigDecimal value, String parentTransactionHash) {
		this.recipient          = recipient;
		this.value               = value;
		this.parentTransactionHash = parentTransactionHash;
		
		String data              = Security.encodeBase64(this.recipient.getPublicKey()) + this.value.toString() + this.parentTransactionHash;
		this.hash                = Security.applySHA256(data);
	}
	
	/**
	 * These coins are mine?
	 * @param publicKey
	 * @return
	 */
	public boolean isMine(PublicKey publicKey) {
		return publicKey == this.recipient.getPublicKey();
	}

	@Override
	public String toString() {
		return String.format("TransactionOutput [hash=%s, recipient=%s, value=%s, parentTransactionHash=%s]"
				,StringUtils.rightPad(hash, 85, " ")
				,recipient.getOwner()
				,value
				,parentTransactionHash
		);
	}

	

}
