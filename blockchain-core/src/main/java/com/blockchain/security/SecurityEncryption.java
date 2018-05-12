package com.blockchain.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author Ualter
 */
@Target({ElementType.FIELD,
	     ElementType.METHOD,
	     ElementType.TYPE,
	     ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface SecurityEncryption {
	
	Algorithm value();
	
	public static enum Algorithm{
		RSA, ECDSA
	}

}
