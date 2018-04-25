package com.blockchain.cryptocurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.junit4.SpringRunner;

import com.blockchain.cryptocurrency.model.Transaction;
import com.blockchain.cryptocurrency.model.Wallet;
import com.blockchain.cryptocurrency.model.WalletImpl;
import com.blockchain.cryptocurrency.pavo.Block;
import com.blockchain.cryptocurrency.pavo.BlockChain;
import com.blockchain.utils.CryptoHashUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlockChainTest {
	
	private Wallet genesisWallet;
	
	@Before
	public void createTheWorld() {
		genesisWallet = BlockChain.initBlockChain();
	}
	
	@Test
	public void firstTestBlockChainTwoBlocks() {
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
			 .addTransaction(transaction7)
			 ;
		BlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
		
		
		double janeWalletBalance    = BlockChain.queryBalance(janeWallet).doubleValue();
		double johnWalletBalance    = BlockChain.queryBalance(johnWallet).doubleValue();
		double genesisWalletBalance = BlockChain.queryBalance(genesisWallet).doubleValue();
		assertEquals("Jane Wallet",140d, janeWalletBalance,0);
		assertEquals("John Wallet",320d, johnWalletBalance,0);
		assertEquals("Genesis Wallet",540d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		
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
		
		janeWalletBalance    = BlockChain.queryBalance(janeWallet).doubleValue();
		johnWalletBalance    = BlockChain.queryBalance(johnWallet).doubleValue();
		genesisWalletBalance = BlockChain.queryBalance(genesisWallet).doubleValue();
		
		assertEquals("Jane Wallet",215d, janeWalletBalance,0);
		assertEquals("John Wallet",475, johnWalletBalance,0);
		assertEquals("Genesis Wallet",310d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		if ( log.isDebugEnabled() ) {
			System.out.println(" *** BLOCKs at testBlockChainTwoBlocks");
			BlockChain.getAllBlocksOfChain().forEach(System.out::println);
			System.out.println("");
		}
		
		// Testing Integrity of the BlockChain (Positive)
		boolean isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A valid BlockChain",true, isValidChain);
		
		// Testing Integrity of the BlockChain (Negative, tampering the value of the one Block, the second Block with the value of 50f)
		block = BlockChain.getAllBlocksOfChain().skip(1).findFirst().get();
		block.getTransactions().get(0).setValue(new BigDecimal(4764)); // change the value of the Transaction from 50 to whatever different
		isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A Not valid BlockChain",false, isValidChain);
		
		// Testing Integrity of the BlockChain (Positive, back to original, the value of the one Block, the second Block with the value of 50f)
		block = BlockChain.getAllBlocksOfChain().skip(1).findFirst().get();
		block.getTransactions().get(0).setValue(new BigDecimal(50));
		isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A Not valid BlockChain",true, isValidChain);
	}

	@Test
	public void secondTestBlockChainSingleBlock() {
		
		Wallet janeWallet    = WalletImpl.build("jane");
		Wallet johnWallet    = WalletImpl.build("john");
		
		
		Block block = new Block();
		Transaction transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction1 = genesisWallet.sendMoney(johnWallet, 10f);
		Transaction transaction2 = johnWallet.sendMoney(janeWallet, 5f);
		Transaction transaction3 = janeWallet.sendMoney(johnWallet, 7f);
		block
		 .addTransaction(transaction0)
		 .addTransaction(transaction1)
		 .addTransaction(transaction2)
		 .addTransaction(transaction3);
		BlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
		
		
		double janeWalletBalance    = BlockChain.queryBalance(janeWallet).doubleValue();
		double johnWalletBalance    = BlockChain.queryBalance(johnWallet).doubleValue();
		double genesisWalletBalance = BlockChain.queryBalance(genesisWallet).doubleValue();
		
		assertEquals("Jane Wallet",48d, janeWalletBalance,0);
		assertEquals("Jonh Wallet",12d, johnWalletBalance,0);
		assertEquals("Genesis Wallet",940d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		if ( log.isDebugEnabled() ) {
			System.out.println(" *** BLOCKs at testBlockChainSingleBlock");
			BlockChain.getAllBlocksOfChain().forEach(System.out::println);
			System.out.println("");
		}

		// Testing Integrity of the BlockChain (Positive)
		boolean isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A valid BlockChain",true, isValidChain);
		
		// Testing Integrity of the BlockChain (Negative, tampering the value of the one Block, the second Block with the value of 50f)
		block = BlockChain.getAllBlocksOfChain().skip(1).findFirst().get();
		block.getTransactions().get(0).setValue(new BigDecimal(4764)); // change the value of the Transaction from 50 to whatever different
		isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A Not valid BlockChain",false, isValidChain);
		
		// Testing Integrity of the BlockChain (Positive, back to original, the value of the one Block, the second Block with the value of 50f)
		block = BlockChain.getAllBlocksOfChain().skip(1).findFirst().get();
		block.getTransactions().get(0).setValue(new BigDecimal(50));
		isValidChain = BlockChain.validateChain(BlockChain.getAllBlocksOfChain());
		assertEquals("A Not valid BlockChain",true, isValidChain);
		
		
		// Testing Merkle Root (Last Test) 
		Block lastBlock = BlockChain.getAllBlocksOfChain().skip(BlockChain.getAllBlocksOfChain().count() - 1).findFirst().get();
		// Recalculating the Merkle Root 
		List<String> hashsOfAllBlocks = BlockChain.getAllBlocksOfChain().map(b -> b.getHash()).collect(Collectors.toList());
		String merkleRoot = CryptoHashUtils.MerkleRoot.calculateMerkleRoot( hashsOfAllBlocks );
		// Should be equals, integrity OK
		if ( log.isDebugEnabled() ) {
			System.out.println("MerkleRoot...........................:" + lastBlock.getMerkleRoot());
			System.out.println("MerkleRoot(Recalculated).............:" + merkleRoot);
		}
		assertEquals("A Valid MerkleRoot",true,lastBlock.getMerkleRoot().equals(merkleRoot));
		
		// Chaging the Second Block Hash
		Block secondBlock = BlockChain.getAllBlocksOfChain().skip(1).findFirst().get();
		secondBlock.setHash("516d5112ea33783f1b8fc40b2e4b2");
		// Recalculating the Merkle Root
		hashsOfAllBlocks = BlockChain.getAllBlocksOfChain().map(b -> b.getHash()).collect(Collectors.toList());
		merkleRoot = CryptoHashUtils.MerkleRoot.calculateMerkleRoot( hashsOfAllBlocks );
		// Should NOT be equals, integrity NOT OK
		if ( log.isDebugEnabled() ) {
			System.out.println("MerkleRoot...........................:" + lastBlock.getMerkleRoot());
			System.out.println("MerkleRoot(Tampered and Recalculated):" + merkleRoot);
		}
		assertEquals("A NOT Valid MerkleRoot",false,lastBlock.getMerkleRoot().equals(merkleRoot));
		
	}
}
