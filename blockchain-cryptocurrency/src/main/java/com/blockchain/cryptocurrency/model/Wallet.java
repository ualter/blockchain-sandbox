package com.blockchain.cryptocurrency.model;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface Wallet {
	
	public PublicKey getPublicKey();
	
	public PrivateKey getPrivateKey();
	
	public void addTransactionOuput(TransactionOutput transactionOutput);
	
	public Transaction sendMoney(PublicKey recipient, float amount);
	
}
