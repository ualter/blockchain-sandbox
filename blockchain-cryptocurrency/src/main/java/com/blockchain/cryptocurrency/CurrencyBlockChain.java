package com.blockchain.cryptocurrency;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.TransactionOutput;
import com.blockchain.cryptocurrency.model.Wallet;
import com.blockchain.cryptocurrency.model.WalletImpl;
import com.blockchain.utils.CryptoHashUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the Global Ledger, the BlockChain of Coins
 * 
 * @author Ualter Junior
 *
 */
@Slf4j
public class CurrencyBlockChain {

	// Unspent Transaction Output, UTXO https://bitcoin.org/en/glossary/unspent-transaction-output)
	private static List<CurrencyBlock>                   BLOCKCHAIN          = new ArrayList<CurrencyBlock>();
	private static Map<String,TransactionOutput> UTXOs               = new HashMap<String,TransactionOutput>(); 
	public static float                          MINIMUM_TRANSACTION = 5;
	public static int                            DIFFICULTY          = 3;
	
	/** 
	 * The Big Ban
	 * @return
	 */
	public static Wallet bigBan() {
		BLOCKCHAIN           = new ArrayList<CurrencyBlock>();
		Wallet genesitWallet = WalletImpl.build();
		CurrencyBlock genesisBlock   = createGenesisBlock(genesitWallet);
		genesisBlock.startMining();
		genesisBlock.setHeight(0);
		BLOCKCHAIN.add( genesisBlock );
		return genesitWallet;
	}
	private static CurrencyBlock createGenesisBlock(Wallet genesisWallet) {
		String genesisHash   = StringUtils.repeat("0", DIFFICULTY);
		Transaction genesisTransaction = new Transaction(genesisWallet, genesisWallet, 1000f, null);
		genesisTransaction.addOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue());
		
		UTXOs.put(genesisTransaction.getOutputs().get(0).getHash(), genesisTransaction.getOutputs().get(0));
		
		CurrencyBlock genesisBlock = new CurrencyBlock();
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
	
	
	public static void addBlock(CurrencyBlock block) {
		// The BlockChain must be started, the "Genesis" Transaction must appear before
		if ( BLOCKCHAIN.isEmpty() ) {
			throw new RuntimeException("The BlockChain must be initialized");
		}
		
		// Get the previous block hash to set this one with it
		CurrencyBlock previousBlock = BLOCKCHAIN.get(BLOCKCHAIN.size() - 1);
		block.setPreviousBlock( previousBlock.getHash() );
		// Calculate its own hash
		block.startMining();
		// Set its position on the chain
		block.setHeight(BLOCKCHAIN.size());
		// Calculate the Merkle Root of the Block
		block.calculateMerkleRoot();
		// Join the Block to the Chain 
		BLOCKCHAIN.add(block);
		// Inform to set the former last Block which are the next now in the chain 
		previousBlock.setNextBlock(block.getHash());
	}

	public static Stream<CurrencyBlock> getAllBlocksOfChain() {
		return BLOCKCHAIN.stream();
	}

	private static String printBlockChainLineBlock(String value, int longitude) {
		if ( value == null ) {
			value = " ";
		}
		int    sizeLeft   = longitude - value.length();
		String fillUp     = StringUtils.repeat(" ", sizeLeft);
		return value + fillUp + "║\n";
	}
	public static void printBlockChain(PrintStream out) {
		DecimalFormat df4      = new DecimalFormat("0000");
		DecimalFormat dfNonce  = new DecimalFormat("###,###,#00");
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		SimpleDateFormat sdf   = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		
		BLOCKCHAIN.forEach(block -> {
			StringBuffer blockToString = new StringBuffer();
			
			String genesis = block.getHeight() == 0 ? "  ► genesis ◄" : " of " + df4.format(BLOCKCHAIN.size() - 1);
			
			int longitude = 83;
			blockToString.append("╔").append(StringUtils.repeat("═",longitude+8)).append("╗").append("\n");
			blockToString.append("║ BLOCK #").append(printBlockChainLineBlock(df4.format(block.getHeight()) + genesis, longitude));
			longitude -= 15;
			blockToString.append("║       ► Hash........: ").append(printBlockChainLineBlock(block.getHash(), longitude));
			blockToString.append("║       ► Previous....: ").append(printBlockChainLineBlock(block.getPreviousBlock(), longitude));
			blockToString.append("║       ► Next........: ").append(printBlockChainLineBlock(block.getNextBlock(), longitude));
			blockToString.append("║       ► TimeStamp...: ").append(printBlockChainLineBlock(sdf.format(block.getTimeStamp()), longitude));
			blockToString.append("║       ► Nonce.......: ").append(printBlockChainLineBlock(dfNonce.format(block.getNonce()), longitude));
			blockToString.append("║       ► Merkle Root.: ").append(printBlockChainLineBlock(block.getMerkleRoot(), longitude));
			blockToString.append("╟─────────┬").append(StringUtils.repeat("─",longitude+13)).append("╢");
			
			AtomicInteger counterTransaction = new AtomicInteger();
			block.getTransactions().forEach(transaction -> {
				
				int longitudeTransction = 68;
				StringBuilder transactionLine = new StringBuilder();
				transactionLine.append("\n║    #").append(df4.format(counterTransaction.get() + 1));
				if ( counterTransaction.incrementAndGet() == block.getTransactions().size() ) {
					transactionLine.append("├").append("─► ");
				} else { 
					transactionLine.append("├").append("─► ");
				}
				transactionLine.append("TRANSACTION: Value..: ");
				transactionLine.append(StringUtils.leftPad(formatter.format(transaction.getValue().floatValue()),11));
				transactionLine.append("   Sender..: " + StringUtils.rightPad(transaction.getSender().getOwner(),7));
				transactionLine.append(" → Recipient..: " + StringUtils.rightPad(transaction.getRecipient().getOwner(),7));
				
				//blockToString.append("\n║    #").append(df4.format(counterTransaction.get() + 1));
//				if ( counterTransaction.incrementAndGet() == block.getTransactions().size() ) {
//					blockToString.append("├").append("─► ").append(transactionLine.toString());
//				} else { 
//					blockToString.append("├").append("─► ").append(transactionLine.toString());
//				}
				blockToString.append(transactionLine.toString());
				blockToString.append(StringUtils.repeat(" ",(transactionLine.length()) - (longitudeTransction+21))).append("║");
				
				
				blockToString.append("\n║         │   INPUTS --→").append(StringUtils.repeat(" ",longitudeTransction)).append("║");
				if ( transaction.getInputs() != null ) {
					transaction.getInputs().forEach(inputs -> {
						StringBuilder line = new StringBuilder();
						line.append("\n║         │             + ");
						line.append(StringUtils.leftPad(formatter.format(inputs.getUTXO().getValue().floatValue()),10)).append(" FROM ");
						line.append(StringUtils.rightPad(inputs.getUTXO().getRecipient().getOwner(), 10));
						
						blockToString.append(line.toString())
							.append(StringUtils.repeat(" ", line.length() - 13)).append("║");
					});
				}
				
				blockToString.append("\n║         │   ←-- OUTPUTS").append(StringUtils.repeat(" ",longitudeTransction-1)).append("║");
				transaction.getOutputs().forEach(outputs -> {
					StringBuilder line = new StringBuilder();
					line.append("\n║         │             - ");
					line.append(StringUtils.leftPad(formatter.format(outputs.getValue().floatValue()),10)).append(" TO   ");
					line.append(StringUtils.rightPad(outputs.getRecipient().getOwner(), 10));
					
					blockToString.append(line.toString()).append(StringUtils.repeat(" ", line.length() - 13)).append("║");
				});
				
				if ( counterTransaction.get() != block.getTransactions().size() ) {
					blockToString.append("\n╟─────────┼──────────────").append(StringUtils.repeat("─", longitudeTransction-1)).append("╢");
				}
			});
			
			blockToString.append("\n╚═════════╧").append(StringUtils.repeat("═",longitude+13)).append("╝").append("\n");
			out.println(blockToString.toString());
		});
	}
	
	/**
	 * Check the integrity of this block, verifying that its Merkle Root value is valid
	 * @param block
	 * @return
	 */
	
	public static boolean validateBlock(CurrencyBlock block) {
		List<String> listHash = new ArrayList<String>();
		
		// Recalculate the Transactions Hash (if the values were not changed, the Hash would be exactly the same - integrity)
		for(Transaction t : block.getTransactions()) {
			String hashTransaction = CryptoHashUtils.applySHA256(
					   CryptoHashUtils.encodeBase64(t.getSender().getPublicKey()) + 
					   CryptoHashUtils.encodeBase64(t.getRecipient().getPublicKey()) +
					   String.valueOf(t.getValue().floatValue()) + 
					   t.getNonce());
			listHash.add(hashTransaction);
		}
		
		String merkleRootOriginal     = block.getMerkleRoot(); 
		String merkleRootRecalculated = CryptoHashUtils.MerkleRoot.calculateMerkleRoot(listHash);
		if ( log.isDebugEnabled() ) {
			System.out.println("MerkleRoot(Original).................:" + merkleRootOriginal);
			System.out.println("MerkleRoot(Recalculated).............:" + merkleRootRecalculated);
			System.out.println(StringUtils.repeat("*", 102));
		}
		return merkleRootOriginal.equals(merkleRootRecalculated);
	}
	
}
