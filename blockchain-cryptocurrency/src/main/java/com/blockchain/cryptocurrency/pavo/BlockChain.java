package com.blockchain.cryptocurrency.pavo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.TransactionOutput;
import com.blockchain.cryptocurrency.model.Wallet;
import com.blockchain.cryptocurrency.model.WalletImpl;

/**
 * This is the Global Ledger, the BlockChain of Coins
 * 
 * @author Ualter
 *
 */
public class BlockChain {

	// Unspent Transaction Output, UTXO https://bitcoin.org/en/glossary/unspent-transaction-output)
	private static List<Block>                   BLOCKCHAIN          = new ArrayList<Block>();
	private static Map<String,TransactionOutput> UTXOs               = new HashMap<String,TransactionOutput>(); 
	public static float                          MINIMUM_TRANSACTION = 5;
	public static int                            DIFFICULTY          = 3;
	
	/** 
	 * The Big Ban
	 * @return
	 */
	public static Wallet initBlockChain() {
		Wallet genesitWallet = WalletImpl.build();
		Block genesisBlock = createGenesisBlock(genesitWallet);
		genesisBlock.calculateHash();
		genesisBlock.setHeight(1);
		BLOCKCHAIN.add( genesisBlock );
		return genesitWallet;
	}
	private static Block createGenesisBlock(Wallet genesisWallet) {
		String genesisHash   = StringUtils.repeat("0", DIFFICULTY);
		Transaction genesisTransaction = new Transaction(genesisWallet, genesisWallet, 1000f, null);
		genesisTransaction.addOutput(genesisTransaction.getRecipient().getPublicKey(), genesisTransaction.getValue());
		
		UTXOs.put(genesisTransaction.getOutputs().get(0).getHash(), genesisTransaction.getOutputs().get(0));
		
		Block genesisBlock = new Block();
		genesisBlock.setPreviousBlock(genesisHash);
		genesisBlock.getTransactions().add(genesisTransaction);
		return genesisBlock;
	}
	
	/**
	 * Add the TransactionsOutput generated at the Transaction as 
	 * Unspent Transaction Output (UTXO), that can be spent as an input in a new transaction
	 * @param transaction
	 */
	public static void addToUTXOs(Transaction t) {
		t.getOutputs().forEach( outputTransaction -> UTXOs.put(outputTransaction.getHash(), outputTransaction) );
	}

	/**
	 * Remove the TransactionsInput created at the Transaction from the UTXOs (they cannot be used anymore, as they were already spent) 
	 * @param transaction
	 */
	public static void removeFromUTXOs(Transaction t) {
		t.getInputs().stream()
			.filter( inputTransaction  -> inputTransaction.getUTXO() != null )
			.forEach( inputTransaction -> UTXOs.remove( inputTransaction.getHash() ));
	}
	
	/**
	 * Validate the Transaction Inputs if they are really available as an unspent transaction in the UTXOs
	 * @param transaction
	 */
	public static void validateTransactionInputWithUTXOs(Transaction t) {
		t.getInputs().forEach(inputTransaction -> inputTransaction.setUTXO( UTXOs.get(inputTransaction.getHash()) ));
	}
	
	/**
	 * Calculate from the UTXOs the balance of an Owner's Coins (using its PublicKey for identification) and deliver to him in format of TransactionOuput 
	 * @param publicKey
	 * @return
	 */
	public static BigDecimal queryBalance(Wallet wallet) {
		if ( BLOCKCHAIN.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		double balance = UTXOs.entrySet()
				.stream()
				.filter( e -> e.getValue().isMine(wallet.getPublicKey())  )
				.mapToDouble(e -> e.getValue().getValue().doubleValue() )
				.sum();
		return new BigDecimal(balance);
	}
	
	/**
	 * Calculate from the UTXOs the balance of an Owner's Coins (using its PublicKey for identification) and deliver to him in format of TransactionOuput 
	 * @param publicKey
	 * @return
	 */
	public static BigDecimal requestBalance(Wallet wallet) {
		if ( BLOCKCHAIN.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		double balance = UTXOs.entrySet()
				.stream()
				.filter( e -> e.getValue().isMine(wallet.getPublicKey())  )
				.peek( e ->  wallet.addTransactionOuput(e.getValue()) )
				.mapToDouble(e -> e.getValue().getValue().doubleValue() )
				.sum();
		return new BigDecimal(balance);
	}
	
	
	public static void addBlock(Block block) {
		// The BlockChain must be started, the "Genesis" Transaction must appear before
		if ( BLOCKCHAIN.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		// Get the previous block hash to set this one with it
		Block previousBlock = BLOCKCHAIN.get(BLOCKCHAIN.size() - 1);
		block.setPreviousBlock( previousBlock.getHash() );
		// Calculate its own hash
		block.calculateHash();
		// Set its position on the chain
		block.setHeight(BLOCKCHAIN.size()+1);
		// Calculate the Merkle Root of the Block
		block.calculateMerkleRoot();
		// It is ready to join the chain 
		BLOCKCHAIN.add(block);
		// Inform to set the former last Block which are the next now in the chain 
		previousBlock.setNextBlock(block.getHash());
	}

	public static Stream<Block> getAllBlocksOfChain() {
		return BLOCKCHAIN.stream();
	}
	
}
