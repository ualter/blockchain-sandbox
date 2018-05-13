package com.blockchain.cryptocurrency.utxo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.blockchain.cryptocurrency.transaction.TransactionOutput;

/**
 * Unspent Transaction Output, <a href="https://bitcoin.org/en/glossary/unspent-transaction-output">UTXO</a>
 *  
 * @see https://bitcoin.org/en/glossary/unspent-transaction-output
 * @author Ualter
 *
 */
@Service
public class UTXOService {
	
	private Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	public void addTransaction(TransactionOutput transactionOutput) {
		UTXOs.put(transactionOutput.getHash(), transactionOutput);
	}
	
	public Consumer<TransactionOutput> addTransaction() {
		Consumer<TransactionOutput> consumer = transactionOutput -> {
			this.addTransaction(transactionOutput);
		};
		return consumer;
	}
	
	public void removeTransaction(String hash) {
		UTXOs.remove(hash);
	}
	
	public TransactionOutput getTransaction(String hash) {
		return UTXOs.get(hash);
	}
	
	public Set<Map.Entry<String,TransactionOutput>> listTransactions() {
		return UTXOs.entrySet();
	}
	
}
