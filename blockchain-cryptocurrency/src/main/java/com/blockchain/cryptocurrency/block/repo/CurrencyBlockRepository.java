package com.blockchain.cryptocurrency.block.repo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.blockchain.cryptocurrency.block.CurrencyBlock;

@Repository
public class CurrencyBlockRepository {
	
	// In-memory
	private List<CurrencyBlock> repository  = new ArrayList<CurrencyBlock>();
	
	public void reset() {
		this.repository = new ArrayList<CurrencyBlock>(); 
	}
	
	public boolean isEmpty() {
		return this.repository.isEmpty();
	}
	
	public void addBlock(CurrencyBlock currencyBlock) {
		this.repository.add(currencyBlock);
	}
	
	public CurrencyBlock getLastBlock() {
		return this.repository.get(this.repository.size() - 1);
	}
	
	public int size() {
		return this.repository.size();
	}
	
	public List<CurrencyBlock> listBlocks() {
		return Collections.unmodifiableList(this.repository);
	}

}
