package com.blockchain.cryptocurrency.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class WalletIdentification {
	
	private String ownerIdentification;
	
	public WalletIdentification(@Value("#{systemProperties['ownerIdentification']}") String ownerIdentification) {
		this.ownerIdentification = ownerIdentification;
	}
	

}
