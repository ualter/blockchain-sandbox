package com.blockchain.cryptocurrency.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


@Service
public class WalletServices {
	
	@Autowired
	ApplicationContext context;
	
	public Wallet createWallet(String owner) {
		return context.getBean(Wallet.class,owner); 
	}
	
	public Wallet createGenesisWallet() {
		return context.getBean(Wallet.class); 
	}

}
