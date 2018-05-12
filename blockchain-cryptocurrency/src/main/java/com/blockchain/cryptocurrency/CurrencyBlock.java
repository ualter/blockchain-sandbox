package com.blockchain.cryptocurrency;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.blockchain.AbstractBlock;
import com.blockchain.cryptocurrency.transaction.Transaction;

import lombok.Getter;

public class CurrencyBlock extends AbstractBlock {
	
	@Getter private List<Transaction> transactions = new ArrayList<Transaction>();
	
	public CurrencyBlock() {
		this.setTimeStamp(Instant.now().toEpochMilli());
	}
	
	public CurrencyBlock addTransaction(Transaction transaction) {
		this.transactions.add(transaction);
		return this;
	}
	
	public void calculateHashBlock() {
		if (transactions == null || transactions.size() == 0) {
			throw new RuntimeException("There's no transaction in this Block to be added to the BlockChain");
		}
		double totalTransaction = transactions.stream().mapToDouble(t -> t.getValue().doubleValue()).sum();
		super.calculateHashBlock(String.valueOf(totalTransaction), CurrencyBlockChain.DIFFICULTY);
	}
	
	public void calculateMerkleRoot() {
		if ( StringUtils.isNotBlank(this.getMerkleRoot()) ) {
			throw new RuntimeException("The Merkle root it was already calculated for this Block");
		}
		if (transactions == null || transactions.size() == 0) {
			throw new RuntimeException("There's no transaction in this Block to calculate the Merkle Root");
		}
		List<String> hashs = this.transactions.stream().map(t -> t.getHash()).collect(Collectors.toList());
		super.calculateMerkleRoot(hashs);
	}
	
	@Override
	public String toString() {
		String genesis = this.getPreviousBlock().equals( StringUtils.repeat("0", CurrencyBlockChain.DIFFICULTY) ) ? " *genesis" : "";
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
				,StringUtils.rightPad(String.valueOf(getNonce()),5)) + genesis;
	}


}
