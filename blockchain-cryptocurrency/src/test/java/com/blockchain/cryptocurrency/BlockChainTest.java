package com.blockchain.cryptocurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.Wallet;
import com.blockchain.cryptocurrency.model.WalletImpl;
import com.blockchain.cryptocurrency.pavo.Block;
import com.blockchain.cryptocurrency.pavo.BlockChain;

@RunWith(SpringRunner.class)
public class BlockChainTest {
	
	private Wallet genesisWallet;
	
	@Before
	public void createTheWorld() {
		genesisWallet = BlockChain.initBlockChain();
	}
	
	@Test
	public void testBlockChainMerkleRootTest() {
		Wallet janeWallet    = WalletImpl.build("jane");
		Wallet johnWallet    = WalletImpl.build("john");
		
		// Even list transaction
		Block block = new Block();
		Transaction transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction1 = genesisWallet.sendMoney(johnWallet, 180f);
		Transaction transaction2 = johnWallet.sendMoney(janeWallet, 25f);
		Transaction transaction3 = janeWallet.sendMoney(johnWallet, 5f);
		Transaction transaction4 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction5 = genesisWallet.sendMoney(johnWallet, 180f);
		Transaction transaction6 = johnWallet.sendMoney(janeWallet, 25f);
		Transaction transaction7 = janeWallet.sendMoney(johnWallet, 5f);
		block = new Block();
		block.addTransaction(transaction0)
			 .addTransaction(transaction1)
			 .addTransaction(transaction2)
			 .addTransaction(transaction3)
			 .addTransaction(transaction4)
			 .addTransaction(transaction5)
			 .addTransaction(transaction6)
			 .addTransaction(transaction7);
		BlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
		System.out.println(block.getMerkleRoot());
		
		// Odd list transaction
		block = new Block();
		transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		transaction1 = genesisWallet.sendMoney(johnWallet, 180f);
		transaction2 = johnWallet.sendMoney(janeWallet, 25f);
		block = new Block();
		block.addTransaction(transaction0)
			 .addTransaction(transaction1)
			 .addTransaction(transaction2);
		BlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
	}

//	@Test
//	public void testBlockChain() {
//		
//		Wallet janeWallet    = WalletImpl.build();
//		Wallet johnWallet    = WalletImpl.build();
//		
//		Transaction transaction = genesisWallet.sendMoney(janeWallet, 50f);
//		Block block = new Block();
//		block.addTransaction(transaction);
//		BlockChain.addBlock(block);
//		
//		transaction = genesisWallet.sendMoney(johnWallet, 275f);
//		block = new Block();
//		block.addTransaction(transaction);
//		BlockChain.addBlock(block);
//		
//		Transaction transaction1 = genesisWallet.sendMoney(janeWallet, 10f);
//		Transaction transaction2 = johnWallet.sendMoney(janeWallet, 25f);
//		Transaction transaction3 = janeWallet.sendMoney(johnWallet, 5f);
//		block = new Block();
//		block.addTransaction(transaction1)
//			 .addTransaction(transaction2)
//			 .addTransaction(transaction3);
//		BlockChain.addBlock(block);
//		
//		BlockChain.getAllBlocksOfChain().forEach(System.out::println);
//		
//		System.out.println( "Jane    Wallet: " + BlockChain.queryBalance(janeWallet));
//		System.out.println( "John    Wallet: " + BlockChain.queryBalance(johnWallet));
//		System.out.println( "Genesis Wallet: " + BlockChain.queryBalance(genesisWallet));
//		
//		assertEquals("Jane Wallet",80d, BlockChain.queryBalance(janeWallet).doubleValue(),0);
//		assertEquals("Jonh Wallet",255d, BlockChain.queryBalance(johnWallet).doubleValue(),0);
//		assertEquals("Genesis Wallet",665d, BlockChain.queryBalance(genesisWallet).doubleValue(),0);
//	}
}
