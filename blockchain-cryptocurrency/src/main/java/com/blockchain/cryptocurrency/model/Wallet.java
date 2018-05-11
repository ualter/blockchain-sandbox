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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.CurrencyBlockChain;
import com.blockchain.security.KeyPairs;
import com.blockchain.security.Security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 *
 */
@Component
@Slf4j
@Data
public class Wallet {
	
	@Autowired
	private Security security;
	
	@Autowired
	private CurrencyBlockChain currencyBlockChain;
	
	private String   owner;
	private KeyPairs keyPairs;
	private Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	
	@Autowired
	public Wallet() {
		this.owner    = "genesis";
		this.keyPairs = KeyPairs.generate();
	}
	
	/**
	 * @param walletOwner - If not null, the keys will be generated automatically the first time, and then subsequently those Keys will be loaded to used again (using the walletOwner as the key)
	 */
	@Autowired
	public Wallet(WalletIdentification ownerIdentification) {
	    if ( StringUtils.isBlank( ownerIdentification.getOwnerIdentification() ) ) {
	    	throw new IllegalArgumentException("Wallet owner identification must no be blank or null!");
		} else {
			this.owner    = ownerIdentification.getOwnerIdentification();
			Path pathFile = Paths.get("src/main/resources/" + ownerIdentification.getOwnerIdentification() + ".keys");
			if ( !Files.exists(pathFile) ) {
				security.saveKeyPairsToFile(pathFile.toAbsolutePath().toString());
			}
			this.keyPairs = security.loadKeyPairsFromFile(pathFile.toAbsolutePath().toString());
		}
	}
	
	public PublicKey getPublicKey() {
		return this.keyPairs.getPublicKey();
	}
	
	public PrivateKey getPrivateKey() {
		return this.keyPairs.getPrivateKey();
	}
	
	public void addTransactionOuput(TransactionOutput transactionOutput) {
		this.UTXOs.put(transactionOutput.getHash(), transactionOutput);
	}
	
	public BigDecimal requestBalance() {
		return currencyBlockChain.requestBalance( this );
	}
	
	public Transaction sendMoney(Wallet recipient, float amount) {
		if ( this.getPublicKey() == recipient ) {
			throw new RuntimeException("Transaction invalid! The sender cannot be the recipient of the own money");
		}
		
		isThereFundsToPerformPayment(amount);
		
		List<TransactionInput> inputs = new ArrayList<TransactionInput>();
		collectAvailableMoneyForPayment(amount, inputs);
		
		Transaction transaction = new Transaction(this, recipient , amount, inputs);
		if ( !transaction.processTransaction() ) {
			throw new RuntimeException("The transaction could no be processed");
		}
		
		removeFromAvailableTheMoneySpent(inputs);
		
		return transaction;
	}

	private void removeFromAvailableTheMoneySpent(List<TransactionInput> inputs) {
		inputs.forEach( ti -> this.UTXOs.remove(ti.getHash()) );
	}

	private void collectAvailableMoneyForPayment(float amount, List<TransactionInput> inputs) {
		float total = 0;
		for ( TransactionOutput to : this.UTXOs.values() ) {
			total += to.getValue().floatValue();
			inputs.add( new TransactionInput(to.getHash()) );
			if( total > amount ) break;
		}
	}

	private void isThereFundsToPerformPayment(float amount) {
		if ( requestBalance().floatValue() < amount) {
			String msg = String.format("Not enough money to \"%s\" to commit this transaction of %01.2f the available funds now are: %01.2f", owner, amount, requestBalance().floatValue());
			log.error(msg);
			throw new RuntimeException(msg);
		}
	}

	public String getOwner() {
		return this.owner;
	}
	
}
