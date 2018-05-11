package com.blockchain.cryptocurrency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.blockchain.security.Security;
import com.blockchain.security.SecurityECDSA;

@Configuration
@ComponentScan("com.blockchain")
public class CurrencyBlockChainConfig {
	
	@Bean
	public Security security() {
		return new SecurityECDSA();
	}
	
}
