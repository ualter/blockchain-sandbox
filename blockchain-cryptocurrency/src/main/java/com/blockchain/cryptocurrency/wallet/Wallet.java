package com.blockchain.cryptocurrency.wallet;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlockChain;
import com.blockchain.cryptocurrency.transaction.Transaction;
import com.blockchain.cryptocurrency.transaction.TransactionInput;
import com.blockchain.cryptocurrency.transaction.TransactionOutput;
import com.blockchain.cryptocurrency.transaction.TransactionServices;
import com.blockchain.security.Security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 *
 */
@Slf4j
@Data
@Component
@Scope("prototype")
public class Wallet {
	
	@Autowired
	private TransactionServices transactionServices;
	
	private Security           security;
	private CurrencyBlockChain currencyBlockChain;
	
	private String  owner;
	private KeyPair keyPairs;
	private Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	@Autowired
	public Wallet(Security security, CurrencyBlockChain currencyBlockChain, String owner) {
		this.security           = security;
		this.currencyBlockChain = currencyBlockChain;
		this.owner              = owner;
		this.generateOrLoadKeyPair();
	}
	
	private void generateOrLoadKeyPair() {
		if ( StringUtils.isBlank( this.owner ) ) {
	    	this.owner    = "genesis";
			this.keyPairs = security.generateKeyPairs();
		} else {
			Path pathFile = Paths.get("src/main/resources/" + this.owner + ".keys");
			if ( !Files.exists(pathFile) ) {
				security.saveKeyPairsToFile(pathFile.toAbsolutePath().toString());
			}
			this.keyPairs = security.loadKeyPairsFromFile(pathFile.toAbsolutePath().toString());
		}
	}
	
	public PublicKey getPublicKey() {
		return this.keyPairs.getPublic();
	}
	
	public PrivateKey getPrivateKey() {
		return this.keyPairs.getPrivate();
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
		
		Transaction transaction = transactionServices.createTransaction(this, recipient , amount, inputs);
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
