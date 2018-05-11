package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.blockchain.cryptocurrency.CurrencyBlockChain;
import com.blockchain.utils.CryptoHashUtils;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Transaction
 * @author Ualter
 *
 */
@Data
@Slf4j
public class Transaction {

	// Just an UUID for unique identification
	private String hash;
	// Amount of coins sent to the recipient
	private BigDecimal value;
	// To guarantee the integrity and protection of this transaction
	private byte[] signature;
	// Wallets of the Transaction (Sender and Recipient)
	@Getter private Wallet sender;
	@Getter private Wallet recipient;
	private String nonce;

	private List<TransactionInput>  inputs  = new ArrayList<TransactionInput>();
	private List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	public Transaction(Wallet sender, Wallet recipient, float value, List<TransactionInput> inputs) {
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
		CurrencyBlockChain.validateTransactionInputWithUTXOs(this);
		
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
		CurrencyBlockChain.addToUTXOs(this);
		// Remove the Transactions Input from the UTXOs (they cannot be used anymore, as they were already spent)
		CurrencyBlockChain.removeFromUTXOs(this);
		
		return true;
	}

	private String calculateTransactionHash() {
		String nonce = UUID.randomUUID().toString();
		this.setNonce(nonce);
		
		return CryptoHashUtils.applySHA256(
			   CryptoHashUtils.encodeBase64(this.sender.getPublicKey()) + 
			   CryptoHashUtils.encodeBase64(this.recipient.getPublicKey()) + 
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
		String data = CryptoHashUtils.encodeBase64(this.sender.getPublicKey()) +
				      CryptoHashUtils.encodeBase64(this.recipient.getPublicKey()) + 
				      this.value.toString();
		//@formatter:on
		return CryptoHashUtils.verifySignature(sender.getPublicKey(), data, signature);
	}
	
	/**
	 * Generate the Signature of this Transaction
	 * @param privateKey
	 */
	private void generateSignature() {
		//@formatter:off
		String data = CryptoHashUtils.encodeBase64(this.sender.getPublicKey()) +
				      CryptoHashUtils.encodeBase64(this.recipient.getPublicKey()) + 
				      this.value.toString();
		//@formatter:on
		this.signature = CryptoHashUtils.sign(this.sender.getPrivateKey(), data);
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

