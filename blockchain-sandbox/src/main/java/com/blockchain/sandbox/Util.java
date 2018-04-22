package com.blockchain.sandbox;

import org.apache.commons.codec.digest.DigestUtils;

public class Util {
	
	public static String applySHA256(String input) {
		return DigestUtils.sha256Hex(input);
	}

}
