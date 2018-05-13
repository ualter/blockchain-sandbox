package com.blockchain.cryptocurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.blockchain.cryptocurrency.block.CurrencyBlock;
import com.blockchain.cryptocurrency.block.CurrencyBlockChain;
import com.blockchain.cryptocurrency.block.CurrencyBlockChainConfig;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinter;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinterType;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinterType.OutputType;
import com.blockchain.cryptocurrency.block.printer.format.CurrencyBlockPrinterDefaultTemplate;
import com.blockchain.cryptocurrency.transaction.Transaction;
import com.blockchain.cryptocurrency.wallet.Wallet;
import com.blockchain.cryptocurrency.wallet.WalletServices;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CurrencyBlockChainConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CurrencyBlockChainTest {
	
	@Autowired
	private CurrencyBlockChain currencyBlockChain;
	
	@Autowired
	private WalletServices walletServices;
	
	@Autowired
	@CurrencyBlockPrinterType(OutputType.Console)
	private CurrencyBlockPrinter printerConsole;
	
	@Autowired
	@CurrencyBlockPrinterType(OutputType.File)
	private CurrencyBlockPrinter printerFile;
	
	private Wallet genesisWallet;
	
	@Before
	public void createTheWorld() {
		genesisWallet = currencyBlockChain.bigBan();
	}
	
	@Test
	public void firstTestBlockChainTwoBlocks() {
		Wallet janeWallet    = walletServices.createWallet("jane");
		Wallet johnWallet    = walletServices.createWallet("john");
		
		// Even list transaction
		CurrencyBlock block = new CurrencyBlock();
		Transaction transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction1 = genesisWallet.sendMoney(johnWallet, 180f);
		Transaction transaction2 = johnWallet.sendMoney(janeWallet, 25f);
		Transaction transaction3 = janeWallet.sendMoney(johnWallet, 5f);
		Transaction transaction4 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction5 = genesisWallet.sendMoney(johnWallet, 180f);
		Transaction transaction6 = johnWallet.sendMoney(janeWallet, 25f);
		Transaction transaction7 = janeWallet.sendMoney(johnWallet, 5f);
		block = new CurrencyBlock();
		block.addTransaction(transaction0)
			 .addTransaction(transaction1)
			 .addTransaction(transaction2)
			 .addTransaction(transaction3)
			 .addTransaction(transaction4)
			 .addTransaction(transaction5)
			 .addTransaction(transaction6)
			 .addTransaction(transaction7)
			 ;
		currencyBlockChain.addBlock(block);
		String blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("Block is valid? (MerkleRoot value is OK?)", true, currencyBlockChain.validateBlock(block));
		
		
		double janeWalletBalance    = currencyBlockChain.queryBalance(janeWallet).doubleValue();
		double johnWalletBalance    = currencyBlockChain.queryBalance(johnWallet).doubleValue();
		double genesisWalletBalance = currencyBlockChain.queryBalance(genesisWallet).doubleValue();
		assertEquals("Jane Wallet",140d, janeWalletBalance,0);
		assertEquals("John Wallet",320d, johnWalletBalance,0);
		assertEquals("Genesis Wallet",540d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		
		// Odd list transaction
		block = new CurrencyBlock();
		transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		transaction1 = genesisWallet.sendMoney(johnWallet, 180f);
		transaction2 = johnWallet.sendMoney(janeWallet, 25f);
		block = new CurrencyBlock();
		block.addTransaction(transaction0)
			 .addTransaction(transaction1)
			 .addTransaction(transaction2);
		currencyBlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
		
		janeWalletBalance    = currencyBlockChain.queryBalance(janeWallet).doubleValue();
		johnWalletBalance    = currencyBlockChain.queryBalance(johnWallet).doubleValue();
		genesisWalletBalance = currencyBlockChain.queryBalance(genesisWallet).doubleValue();
		
		assertEquals("Jane Wallet",215d, janeWalletBalance,0);
		assertEquals("John Wallet",475, johnWalletBalance,0);
		assertEquals("Genesis Wallet",310d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		if ( log.isDebugEnabled() ) {
			System.out.println(" *** BLOCKs at testBlockChainTwoBlocks");
			currencyBlockChain.streamBlockChain().forEach(System.out::println);
			System.out.println("");
		}
		
		blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("Block is valid? (MerkleRoot value is OK?)", true, currencyBlockChain.validateBlock(block));
		
		// Testing Integrity of the Block (Negative), change the value of one Transaction without re-calculate the MerkleRoot)
		// Change the value of the Transaction from 25 to whatever different
		block.getTransactions().stream().skip(1).skip(1).findFirst().get().setValue(new BigDecimal(44));
		assertEquals("Block is valid? (MerkleRoot value is OK?) In this case it shouldn't", false, currencyBlockChain.validateBlock(block));
		
		printerFile.print(currencyBlockChain.listBlockChain());
		printerConsole.print(currencyBlockChain.listBlockChain());
	}

	@Test
	public void secondTestBlockChainSingleBlock() {
		Wallet janeWallet    = walletServices.createWallet("jane");
		Wallet johnWallet    = walletServices.createWallet("john");
		
		CurrencyBlock block = new CurrencyBlock();
		Transaction transaction0 = genesisWallet.sendMoney(janeWallet, 50f);
		Transaction transaction1 = genesisWallet.sendMoney(johnWallet, 10f);
		Transaction transaction2 = johnWallet.sendMoney(janeWallet, 5f);
		Transaction transaction3 = janeWallet.sendMoney(johnWallet, 7f);
		block
		 .addTransaction(transaction0)
		 .addTransaction(transaction1)
		 .addTransaction(transaction2)
		 .addTransaction(transaction3);
		currencyBlockChain.addBlock(block);
		assertNotNull("Merkle Root is Null?",block.getMerkleRoot());
		
		
		double janeWalletBalance    = currencyBlockChain.queryBalance(janeWallet).doubleValue();
		double johnWalletBalance    = currencyBlockChain.queryBalance(johnWallet).doubleValue();
		double genesisWalletBalance = currencyBlockChain.queryBalance(genesisWallet).doubleValue();
		
		assertEquals("Jane Wallet",48d, janeWalletBalance,0);
		assertEquals("Jonh Wallet",12d, johnWalletBalance,0);
		assertEquals("Genesis Wallet",940d, genesisWalletBalance,0);
		assertEquals("Total Balance BlockChain",1000d, (janeWalletBalance+johnWalletBalance+genesisWalletBalance),0);
		
		if ( log.isDebugEnabled() ) {
			System.out.println(" *** BLOCKs at testBlockChainSingleBlock");
			currencyBlockChain.streamBlockChain().forEach(System.out::println);
			System.out.println("");
		}

		// Testing Integrity of the BlockChain (Positive)
		String blockMerkleRoot = block.getMerkleRoot();
		assertNotNull("Merkle Root is Null?",blockMerkleRoot);
		assertEquals("A valid BlockChain",true, currencyBlockChain.validateBlock(block));
		
		
		// Testing Integrity of the BlockChain (Negative, tampering the value of one Transaction, the second one with the value of 50f)
		// change the value of the Transaction from 10 to whatever different
		block.getTransactions().stream().skip(1).findFirst().get().setValue(new BigDecimal(4764));
		assertEquals("A Not valid BlockChain",false, currencyBlockChain.validateBlock(block));
		
		// Testing Integrity of the BlockChain (Positive, back to original (10), the value of the Transaction changed before)
		block.getTransactions().stream().skip(1).findFirst().get().setValue(new BigDecimal(10));
		assertEquals("A Not valid BlockChain",true, currencyBlockChain.validateBlock(block));
		
		printerFile.print(currencyBlockChain.listBlockChain());
		printerConsole.print(currencyBlockChain.listBlockChain());
	}
}
