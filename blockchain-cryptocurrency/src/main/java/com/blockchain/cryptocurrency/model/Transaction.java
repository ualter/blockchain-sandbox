package com.blockchain.cryptocurrency.model;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.blockchain.cryptocurrency.pavo.BlockChain;
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
	// Sender's public key
	private PublicKey senderPublicKey;
	// Receiver's public key
	private PublicKey recipientPublickey;
	// To guarantee the integrity and protection of this transaction
	private byte[] signature;
	// Wallets of the Transaction
	@Getter private Wallet sender;
	@Getter private Wallet recipient;

	private List<TransactionInput>  inputs  = new ArrayList<TransactionInput>();
	private List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	public Transaction(Wallet sender, Wallet recipient, float value, List<TransactionInput> inputs) {
		this.sender             = sender;
		this.recipient          = recipient;
		this.value              = BigDecimal.valueOf(value);
		this.senderPublicKey    = sender.getPublicKey();
		this.recipientPublickey = recipient.getPublicKey();
		this.inputs             = inputs;
		this.calculateTransactionHash();
		this.generateSignature();
	}
	
	public void addOutput(PublicKey recipient, BigDecimal value) {
		TransactionOutput transactionOutput = new TransactionOutput(recipient, value, this.hash);
		this.outputs.add(transactionOutput);
	}
	public void addOutput(PublicKey recipient, float value) {
		this.addOutput(recipient, BigDecimal.valueOf(value));
	}

	public boolean processTransaction() {
		if (!checkSignature()) {
			log.error("Error, signature do not match!");
			return false;
		}

		// Check the transaction inputs, verifying that they were not spent and, use it so... otherwise discard it
		BlockChain.validateTransactionInputWithUTXOs(this);
		
		// Checks the value of transaction
		float totalTransaction = processTotalTransaction(); 
		if (  totalTransaction < BlockChain.MINIMUM_TRANSACTION ) {
			log.warn("Total of Transaction is too small: {}, the minimum amount allowed is:{}", totalTransaction, BlockChain.MINIMUM_TRANSACTION);
			return false;
		}
		
		// What is left after pay the sent value (This is the change for the Sender) 
		float leftOver = totalTransaction - this.value.floatValue();
		this.hash      = calculateTransactionHash();
		
		// Send the coins to the Recipient
		this.outputs.add(new TransactionOutput(this.recipientPublickey, value, this.hash));
		// Return the change (the leftOver) back to the Sender
		this.outputs.add(new TransactionOutput(this.senderPublicKey, BigDecimal.valueOf(leftOver),this.hash));
		
		// Add the Transactions Output generated in this transaction as Unspent Transaction Output (UTXO), that can be spent as an input in a new transaction
		BlockChain.addToUTXOs(this);
		// Remove the Transactions Input from the UTXOs (they cannot be used anymore, as they were already spent)
		BlockChain.removeFromUTXOs(this);
		
		return true;
	}

	private String calculateTransactionHash() {
		String variation = UUID.randomUUID().toString();
		
		return CryptoHashUtils.applySHA256(
			   CryptoHashUtils.encodeBase64(this.senderPublicKey) + 
			   CryptoHashUtils.encodeBase64(this.recipientPublickey) + 
			   this.value.toString() + 
			   variation
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
		String data = CryptoHashUtils.encodeBase64(this.senderPublicKey) +
				      CryptoHashUtils.encodeBase64(this.recipientPublickey) + 
				      this.value.toString();
		//@formatter:on
		return CryptoHashUtils.verifySignature(senderPublicKey, data, signature);
	}
	
	/**
	 * Generate the Signature of this Transaction
	 * @param privateKey
	 */
	private void generateSignature() {
		//@formatter:off
		String data = CryptoHashUtils.encodeBase64(this.senderPublicKey) +
				      CryptoHashUtils.encodeBase64(this.recipientPublickey) + 
				      this.value.toString();
		//@formatter:on
		this.signature = CryptoHashUtils.sign(this.sender.getPrivateKey(), data);
	}

	
	
}

