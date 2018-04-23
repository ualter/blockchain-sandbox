package com.blockchain.cryptocurrency.pavo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.blockchain.AbstractBlock;
import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.utils.CryptoHashUtils;

import lombok.Getter;

public class Block extends AbstractBlock {
	
	@Getter private List<Transaction> transactions = new ArrayList<Transaction>();
	
	public Block() {
		this.setTimeStamp(Instant.now().toEpochMilli());
	}
	
	public Block addTransaction(Transaction transaction) {
		this.transactions.add(transaction);
		return this;
	}
	
	public void calculateHash() {
		if (transactions == null || transactions.size() == 0) {
			throw new RuntimeException("There's no transaction in this Block to be added to the BlockChain");
		}
		double totalTransaction = transactions.stream().mapToDouble(t -> t.getValue().doubleValue()).sum();
		super.calculateHash(String.valueOf(totalTransaction), BlockChain.DIFFICULTY);
	}
	
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("0000");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		double total = transactions.stream().mapToDouble(d -> d.getValue().doubleValue()).sum();
		int totalRegister = transactions.size();
		return String.format("Block [#%s | Hash=%s | PreviousBlock=%s | NextBlock=%s | TimeStamp=%s | totalTransaction=%s | QtdeTransaction=%s | Nonce=%s]"
				,StringUtils.rightPad(df.format(getHeight()),4)
				,StringUtils.rightPad(getHash(),65)
				,StringUtils.rightPad(getPreviousBlock(),65)
				,StringUtils.rightPad(getNextBlock() == null ? " " : getNextBlock(),65)
				,StringUtils.rightPad(formatter.format(getTimeStamp()),22)
				,StringUtils.leftPad(String.valueOf(totalRegister), 4)
				,StringUtils.leftPad(String.valueOf(total), 12)
				,StringUtils.rightPad(String.valueOf(getNonce()),5));
	}

	/**
	 * This Hash (Merkle Root) it can be used to prove the integrity of all of the transactions in this Block
	 */
	public void calculateMerkleRoot() {
		// Only one transaction, then there is nothing more to do, the only transaction hash is the Merkle root transaction
		if ( this.transactions.size() == 1 ) {
			this.setMerkleRoot( CryptoHashUtils.applySHA256( this.transactions.get(0).getHash() ) );
		} else {
			List<String> hashs = this.transactions.stream().map(t -> t.getHash()).collect(Collectors.toList());
			super.calculateMerkleRoot(hashs);
		}
	}

}
