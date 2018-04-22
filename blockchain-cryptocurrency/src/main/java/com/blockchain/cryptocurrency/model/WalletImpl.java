package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockchain.cryptocurrency.pavo.BlockChain;
import com.blockchain.utils.KeyPairs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WalletImpl implements Wallet {
	
	private KeyPairs keyPairs;
	private Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static Wallet build() {
		return new WalletImpl();
	}
	
	private WalletImpl() {
		this.keyPairs = KeyPairs.generate();
	}
	
	@Override
	public PublicKey getPublicKey() {
		return this.keyPairs.getPublicKey();
	}
	
	@Override
	public PrivateKey getPrivateKey() {
		return this.keyPairs.getPrivateKey();
	}
	
	@Override
	public void addTransactionOuput(TransactionOutput transactionOutput) {
		this.UTXOs.put(transactionOutput.getHash(), transactionOutput);
	}
	
	public BigDecimal requestBalance() {
		return BlockChain.requestBalance( this );
	}
	
	public Transaction sendMoney(PublicKey recipient, float amount) {
		if ( this.getPublicKey() == recipient ) {
			throw new RuntimeException("The sender cannot be the recipient of the money");
		}
		
		// Check if there are funds to pay the amount
		float balance = requestBalance().floatValue(); 
		if ( balance < amount) {
			log.warn("Not enough money to commit this transaction of {}, the available funds now are: {}", amount, balance);
			return null;
		}
		
		// Collect the money from his/her UTXOs available (BlockChain) when requested the Balance 
		List<TransactionInput> inputs = new ArrayList<TransactionInput>();
		float total = 0;
		for ( TransactionOutput to : this.UTXOs.values() ) {
			total += to.getValue().floatValue();
			inputs.add( new TransactionInput(to.getHash()) );
			if( total > amount ) break;
		}
		
		// Create the transaction with the PublicKey of the Sender and Recipient, the amount and the Inputs from the UTXOs
		Transaction transaction = new Transaction(this.getPublicKey(), recipient , amount, inputs);
		// Sign the transaction with its PrivateKey (Sender)
		transaction.generateSignature( this.getPrivateKey() );
		transaction.processTransaction();
		
		// Remove from the Sender's UTXOs (BlockChain) the Inputs sent to the Recipient 
		inputs.forEach( ti -> this.UTXOs.remove(ti.getHash()) );
		
		return transaction;
	}

	

	
		
	
}
