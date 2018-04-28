package com.blockchain.cryptocurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlockChainTest {
	
	private Wallet genesisWallet;
	
	@Before
	public void createTheWorld() {
		genesisWallet = BlockChain.bigBan();
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
		String blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("Block is valid? (MerkleRoot value is OK?)", true, BlockChain.validateBlock(block));
		
		
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
		
		blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("Block is valid? (MerkleRoot value is OK?)", true, BlockChain.validateBlock(block));
		
		// Testing Integrity of the Block (Negative), change the value of one Transaction without re-calculate the MerkleRoot)
		// Change the value of the Transaction from 25 to whatever different
		block.getTransactions().stream().skip(1).skip(1).findFirst().get().setValue(new BigDecimal(44));
		assertEquals("Block is valid? (MerkleRoot value is OK?) In this case it shouldn't", false, BlockChain.validateBlock(block));
		
		
		BlockChain.printBlockChain(System.out);
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
		String blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("A valid BlockChain",true, BlockChain.validateBlock(block));
		
		
		// Testing Integrity of the BlockChain (Negative, tampering the value of one Transaction, the second one with the value of 50f)
		// change the value of the Transaction from 10 to whatever different
		block.getTransactions().stream().skip(1).findFirst().get().setValue(new BigDecimal(4764));
		assertEquals("A Not valid BlockChain",false, BlockChain.validateBlock(block));
		
		// Testing Integrity of the BlockChain (Positive, back to original (10), the value of the Transaction changed before)
		block.getTransactions().stream().skip(1).findFirst().get().setValue(new BigDecimal(10));
		assertEquals("A Not valid BlockChain",true, BlockChain.validateBlock(block));
	}
}
