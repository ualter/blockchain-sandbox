package com.blockchain.cryptocurrency.transaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.wallet.Wallet;

@Component
public class TransactionServices {
	
	@Autowired
	ApplicationContext context;
	
	public Transaction createTransaction(Wallet sender, Wallet recipient, float value, List<TransactionInput> inputs) {
		return context.getBean(Transaction.class, sender,  recipient, value, inputs);
	}
	
}
