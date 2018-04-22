package com.blockchain.cryptocurrency;

import org.junit.Test;

import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.Wallet;
import com.blockchain.cryptocurrency.model.WalletImpl;
import com.blockchain.cryptocurrency.pavo.Block;
import com.blockchain.cryptocurrency.pavo.BlockChain;

public class BlockChainTest {
	

	@Test
	public void testBlockChain() {
		
		Wallet genesisWallet = BlockChain.initBlockChain();
		Wallet janeWallet    = WalletImpl.build();
		
		
		Transaction transaction = genesisWallet.sendMoney(janeWallet.getPublicKey(), 50f);
		Block block = new Block();
		block.addTransaction(transaction);
		BlockChain.addBlock(block);
		
		BlockChain.getAllBlocksOfChain().forEach(System.out::println);
		
		System.out.println( "Jane    Wallet: " + BlockChain.queryBalance(janeWallet));
		System.out.println( "Genesis Wallet: " + BlockChain.queryBalance(genesisWallet));
		
		
	}
}
