package com.blockchain.cryptocurrency.transaction;

import lombok.Data;

/**
 * 
 * <a href="https://bitcoin.org/en/glossary/unspent-transaction-output" target="_blank">UTXO</a> = An Unspent Transaction Output 
 * @author Ualter
 *
 */
@Data
public class TransactionInput {
	
	private String hash;
	// The Unspent Transaction Output, see:https://bitcoin.org/en/glossary/unspent-transaction-output
	private TransactionOutput UTXO;
	
	public TransactionInput(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return String.format("TransactionInput [hash=%s, UTXO=%s]", hash, UTXO);
	}

	
}
