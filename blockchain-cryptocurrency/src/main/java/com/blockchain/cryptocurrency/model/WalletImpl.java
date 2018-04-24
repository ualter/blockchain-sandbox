package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.blockchain.cryptocurrency.pavo.BlockChain;
import com.blockchain.utils.CryptoHashUtils;
import com.blockchain.utils.KeyPairs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WalletImpl implements Wallet {
	
	private KeyPairs keyPairs;
	private Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static Wallet build(boolean persistedKeyPairs, String walletOwner) {
		return new WalletImpl(persistedKeyPairs, walletOwner);
	}
	
	public static Wallet build() {
		return new WalletImpl(false, null);
	}
	
	private WalletImpl(boolean persistedKeyPairs, String walletOwner) {
		if ( StringUtils.isBlank(walletOwner) ) {
			throw new IllegalArgumentException("Wallet Owner must be informed when they key pairs are being persisted");
		}
		if ( !persistedKeyPairs ) {
			this.keyPairs = KeyPairs.generate();
		} else {
			Path pathFile = Paths.get("src/main/resources/" + walletOwner + ".keys");
			if ( !Files.exists(pathFile) ) {
				CryptoHashUtils.saveKeyECSDAPairsInFile(pathFile.toAbsolutePath().toString());
			}
			CryptoHashUtils.loadKeyECSDAPairsInFile(pathFile.toAbsolutePath().toString());
		}
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
	
	@Override
	public Transaction sendMoney(Wallet recipient, float amount) {
		if ( this.getPublicKey() == recipient ) {
			throw new RuntimeException("Transaction invalid! The sender cannot be the recipient of the own money");
		}
		
		// Check if there are funds to pay the amount
		float balance = requestBalance().floatValue(); 
		if ( balance < amount) {
			String msg = String.format("Not enough money to commit this transaction of %01.2f the available funds now are: %01.2f", amount, balance);
			log.error(msg);
			throw new RuntimeException(msg);
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
		Transaction transaction = new Transaction(this, recipient , amount, inputs);
		if ( !transaction.processTransaction() ) {
			throw new RuntimeException("The transaction could no be processed");
		}
		
		// Remove from the Sender's UTXOs (BlockChain) the Inputs sent to the Recipient 
		inputs.forEach( ti -> this.UTXOs.remove(ti.getHash()) );
		
		return transaction;
	}
	
}
