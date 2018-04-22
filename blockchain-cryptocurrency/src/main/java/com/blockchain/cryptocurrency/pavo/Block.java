package com.blockchain.cryptocurrency.pavo;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.blockchain.AbstractBlock;
import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.TransactionInput;
import com.blockchain.utils.CryptoHashUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Block extends AbstractBlock {
	
	@Getter private List<Transaction> transactions = new ArrayList<Transaction>();
	
	public Block() {
		this.setTimeStamp(Instant.now().toEpochMilli());
		this.setHash(calculateHash());
	}
	
	public void addTransaction(Transaction transaction) {
		this.transactions.add(transaction);
	}
	
	private String calculateHash() {
		double totalTransaction = transactions.stream().mapToDouble(t -> t.getValue().doubleValue()).sum();
		int    nonce            = -1;
		String calculatedhash   = "";
		
		while ( !isHashValid(calculatedhash, BlockChain.DIFFICULTY) ) {
			calculatedhash = CryptoHashUtils.applySHA256(
					  totalTransaction
					+ Long.toString(this.getTimeStamp())  
					+ this.getPreviousHash()
					+ (nonce+=1)
					//+ this.getMerkleRoot()
			);
		}
		
		log.debug("Hash {} calculated with nonce {}", calculatedhash, nonce);
		return calculatedhash;
	}

	@Override
	public String toString() {
		double total = transactions.stream().mapToDouble(d -> d.getValue().doubleValue()).sum();
		return String.format("Block [%s, total=%.2f]", getHash(), total );
	}
	
	
	
	

	
	

}
