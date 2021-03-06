package com.blockchain.cryptocurrency.block;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.blockchain.cryptocurrency.transaction.Transaction;
import com.blockchain.cryptocurrency.transaction.TransactionInput;
import com.blockchain.cryptocurrency.wallet.Wallet;
import com.blockchain.security.Security;
import com.blockchain.security.SecurityEncryption;
import com.blockchain.security.SecurityEncryption.Algorithm;

@Configuration
@ComponentScan("com.blockchain")
public class CurrencyBlockChainConfig {
	
	@Autowired
	@SecurityEncryption(Algorithm.ECDSA)
	private Security security;
		
	@Autowired
	private CurrencyBlockChain currencyBlockChain;

	@Bean
	@Scope("prototype")
	public Wallet wallet(String owner) {
		return new Wallet(security, currencyBlockChain, owner);
	}
	
	@Bean
	@Scope("prototype")
	public Wallet wallet() {
		return new Wallet(security, currencyBlockChain, null);
	}
	
	@Bean
	@Scope("prototype")
	public Transaction transaction(Wallet sender, Wallet recipient, float value, List<TransactionInput> inputs) {
		return new Transaction(security, currencyBlockChain, sender,  recipient, value, inputs);
	}
	
}
