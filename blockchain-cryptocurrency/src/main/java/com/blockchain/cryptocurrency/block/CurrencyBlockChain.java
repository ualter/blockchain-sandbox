package com.blockchain.cryptocurrency.block;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.repo.CurrencyBlockRepository;
import com.blockchain.cryptocurrency.transaction.Transaction;
import com.blockchain.cryptocurrency.transaction.TransactionServices;
import com.blockchain.cryptocurrency.utxo.UTXOServices;
import com.blockchain.cryptocurrency.wallet.Wallet;
import com.blockchain.cryptocurrency.wallet.WalletServices;
import com.blockchain.security.Security;
import com.blockchain.utils.MerkleRoot;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 */
@Component
@Slf4j
public class CurrencyBlockChain {
	
	@Autowired
	private WalletServices walletServices;
	
	@Autowired
	private TransactionServices transactionServices;
	
	@Autowired
	private CurrencyBlockRepository currencyBlockRepository;
	
	@Autowired
	private UTXOServices utxoServices;
	
	public static float                          MINIMUM_TRANSACTION = 5;
	public static int                            DIFFICULTY          = 3;
	
	public Wallet bigBan() {
		currencyBlockRepository.reset();
		Wallet genesitWallet         = walletServices.createGenesisWallet();
		CurrencyBlock genesisBlock   = createGenesisBlock(genesitWallet);
		genesisBlock.calculateHashBlock();
		genesisBlock.setHeight(0);
		currencyBlockRepository.addBlock( genesisBlock );
		return genesitWallet;
	}
	
	private CurrencyBlock createGenesisBlock(Wallet genesisWallet) {
		String genesisHash   = StringUtils.repeat("0", DIFFICULTY);
		Transaction genesisTransaction = transactionServices.createTransaction(genesisWallet, genesisWallet, 1000f, null);
		genesisTransaction.addOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue());
		utxoServices.addTransaction(genesisTransaction.getOutputs().get(0));
		CurrencyBlock genesisBlock = new CurrencyBlock();
		genesisBlock.setPreviousBlock(genesisHash);
		genesisBlock.getTransactions().add(genesisTransaction);
		return genesisBlock;
	}
	
	/**
	 * Result of Transactions (Output) that can be spent in the future (as Input for new Transactions), added to Unspent Transaction Output (UTXO)
	 */
	public void addToUTXOs(Transaction t) {
		t.getOutputs().forEach(utxoServices.addTransaction());
	}

	/**
	 * The Transaction were spent, so it cannot be at the UTXOs anymore (cannot be used anymore) 
	 */
	public void removeFromUTXOs(Transaction t) {
		t.getInputs().stream()
			.filter( inputTransaction  -> inputTransaction.getUTXO() != null )
			.forEach( inputTransaction -> utxoServices.removeTransaction(inputTransaction.getHash()) );
	}
	
	/**
	 * The Input Transaction are really available as an unspent transaction in the UTXOs (can be spent)?
	 */
	public void validateTransactionInputWithUTXOs(Transaction t) {
		t.getInputs().forEach(inputTransaction -> inputTransaction.setUTXO( utxoServices.getTransaction(inputTransaction.getHash()) ));
	}
	
	/**
	 * Calculate from the UTXOs the balance of an Owner's Coins (using its PublicKey for identification) and deliver to him in format of TransactionOuput 
	 * @param publicKey
	 * @return
	 */
	public BigDecimal queryBalance(Wallet wallet) {
		if ( currencyBlockRepository.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		double balance = utxoServices.listTransactions()
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
	public BigDecimal requestBalance(Wallet wallet) {
		if ( currencyBlockRepository.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		double balance = utxoServices.listTransactions()
				.stream()
				.filter( e -> e.getValue().isMine(wallet.getPublicKey())  )
				.peek( e ->  wallet.addTransactionOuput(e.getValue()) )
				.mapToDouble(e -> e.getValue().getValue().doubleValue() )
				.sum();
		return new BigDecimal(balance);
	}
	
	
	public void addBlock(CurrencyBlock block) {
		// The BlockChain must be started, the "Genesis" Transaction must appear before
		if ( currencyBlockRepository.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		// Get the previous block hash to set this one with it
		CurrencyBlock previousBlock = currencyBlockRepository.getLastBlock();
		block.setPreviousBlock( previousBlock.getHash() );
		// Calculate its own hash
		block.calculateHashBlock();
		// Set its position on the chain
		block.setHeight(currencyBlockRepository.size());
		// Calculate the Merkle Root of the Block
		block.calculateMerkleRoot();
		// Join the Block to the Chain 
		currencyBlockRepository.addBlock(block);
		// Inform to set the former last Block which are the next now in the chain 
		previousBlock.setNextBlock(block.getHash());
	}

	public Stream<CurrencyBlock> streamBlockChain() {
		return currencyBlockRepository.listBlocks().stream();
	}
	
	public List<CurrencyBlock> listBlockChain() {
		return currencyBlockRepository.listBlocks();
	}

	/**
	 * Check the integrity of this block, verifying that its Merkle Root value is valid
	 * @param block
	 * @return
	 */
	public boolean validateBlock(CurrencyBlock block) {
		List<String> listHash = new ArrayList<String>();
		
		// Recalculate the Transactions Hash (if the values were not changed, the Hash would be exactly the same - integrity)
		for(Transaction t : block.getTransactions()) {
			String hashTransaction = Security.applySHA256(
					   Security.encodeBase64(t.getSender().getPublicKey()) + 
					   Security.encodeBase64(t.getRecipient().getPublicKey()) +
					   String.valueOf(t.getValue().floatValue()) + 
					   t.getNonce());
			listHash.add(hashTransaction);
		}
		
		String merkleRootOriginal     = block.getMerkleRoot(); 
		String merkleRootRecalculated = MerkleRoot.calculate(listHash);
		if ( log.isDebugEnabled() ) {
			System.out.println("MerkleRoot(Original).................:" + merkleRootOriginal);
			System.out.println("MerkleRoot(Recalculated).............:" + merkleRootRecalculated);
			System.out.println(StringUtils.repeat("*", 102));
		}
		return merkleRootOriginal.equals(merkleRootRecalculated);
	}
	
}
