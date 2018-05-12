package com.blockchain.cryptocurrency.transaction;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlockChain;
import com.blockchain.cryptocurrency.wallet.Wallet;
import com.blockchain.security.Security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ualter
 */
@Slf4j
@Component
@Scope("prototype")
public class Transaction {

	private Security           security;
	private CurrencyBlockChain currencyBlockChain;
	
	@Getter private String hash;
	@Getter @Setter private BigDecimal value;
	@Getter private byte[] signature;
	@Getter private Wallet sender;
	@Getter private Wallet recipient;
	@Getter private String nonce;

	@Getter private List<TransactionInput>  inputs  = new ArrayList<TransactionInput>();
	@Getter private List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	@Autowired
	public Transaction(Security security, CurrencyBlockChain currencyBlockChain, Wallet sender, Wallet recipient, float value, List<TransactionInput> inputs) {
		this.security           = security;
		this.currencyBlockChain = currencyBlockChain;
		this.sender             = sender;
		this.recipient          = recipient;
		this.value              = BigDecimal.valueOf(value);
		this.inputs             = inputs;
		this.hash               = this.calculateTransactionHash();
		this.generateSignature();
	}
	
	public void addOutput(Wallet recipient, BigDecimal value) {
		TransactionOutput transactionOutput = new TransactionOutput(recipient, value, this.hash);
		this.outputs.add(transactionOutput);
	}
	public void addOutput(Wallet recipient, float value) {
		this.addOutput(recipient, BigDecimal.valueOf(value));
	}

	public boolean processTransaction() {

		if (!checkSignature()) {
			log.error("Error, signature do not match!");
			return false;
		}

		// Check the transaction inputs, verifying that they were not spent and, use it so... otherwise discard it
		currencyBlockChain.validateTransactionInputWithUTXOs(this);
		
		// Checks the value of transaction
		float totalTransaction = processTotalTransaction(); 
		if (  totalTransaction < CurrencyBlockChain.MINIMUM_TRANSACTION ) {
			log.warn("Total of Transaction is too small: {}, the minimum amount allowed is:{}", totalTransaction, CurrencyBlockChain.MINIMUM_TRANSACTION);
			return false;
		}
		
		// What is left after pay the sent value (This is the change for the Sender) 
		float leftOver = totalTransaction - this.value.floatValue();
		if ( StringUtils.isBlank(this.hash) ) {
			throw new RuntimeException("The Hash of the Transaction were not calculated yet, must be done before it be processed");
		}
		
		// Send the coins to the Recipient
		this.outputs.add(new TransactionOutput(this.recipient, value, this.hash));
		// Return the change (the leftOver) back to the Sender
		this.outputs.add(new TransactionOutput(this.sender, BigDecimal.valueOf(leftOver),this.hash));
		
		// Add the Transactions Output generated in this transaction as Unspent Transaction Output (UTXO), that can be spent as an input in a new transaction
		currencyBlockChain.addToUTXOs(this);
		// Remove the Transactions Input from the UTXOs (they cannot be used anymore, as they were already spent)
		currencyBlockChain.removeFromUTXOs(this);
		
		return true;
	}

	private String calculateTransactionHash() {
		String nonce = UUID.randomUUID().toString();
		this.nonce = nonce;
		
		return Security.applySHA256(
			   Security.encodeBase64(this.sender.getPublicKey()) + 
			   Security.encodeBase64(this.recipient.getPublicKey()) + 
			   String.valueOf(this.value.floatValue()) + 
			   nonce
		);
	}
	
	private float processTotalTransaction() {
		//@formatter:off
		float total = (float) this.inputs.stream()
										 .filter(i -> i.getUTXO() != null)
										 .mapToDouble(i -> i.getUTXO().getValue().doubleValue())
										 .sum();
		//@formatter:on
		return total;
	}

	/**
	 * Check sender's signature, is it really from him? 
	 * I mean... generated with his unique and protected PrivateKey, and did not suffer tampering
	 * 
	 * @return
	 */
	private boolean checkSignature() {
		//@formatter:off
		String data = Security.encodeBase64(this.sender.getPublicKey()) +
				      Security.encodeBase64(this.recipient.getPublicKey()) + 
				      this.value.toString();
		//@formatter:on
		return security.verifySignature(sender.getPublicKey(), data, signature);
	}
	
	/**
	 * Generate the Signature of this Transaction
	 * @param privateKey
	 */
	private void generateSignature() {
		//@formatter:off
		String data = Security.encodeBase64(this.sender.getPublicKey()) +
				      Security.encodeBase64(this.recipient.getPublicKey()) + 
				      this.value.toString();
		//@formatter:on
		this.signature = security.sign(this.sender.getPrivateKey(), data);
	}

	@Override
	public String toString() {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction  value=");
		builder.append(StringUtils.leftPad(formatter.format(value.floatValue()),11));
		builder.append("│ Sender=" + StringUtils.rightPad(this.sender.getOwner(),10));
		builder.append("│ Recipient=" + StringUtils.rightPad(this.recipient.getOwner(),10));
		builder.append("│ inputs=");
		builder.append(inputs);
		builder.append("│ outputs=");
		builder.append(outputs);
		builder.append("]");
		return builder.toString();
	}
	
	
}

