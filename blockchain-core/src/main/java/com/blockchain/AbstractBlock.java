package com.blockchain;

import org.apache.commons.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractBlock {
	
	@Getter @Setter(AccessLevel.PROTECTED) private String hash;
	@Getter @Setter(AccessLevel.PUBLIC)    private String previousHash;
	@Getter @Setter(AccessLevel.PROTECTED) private long   timeStamp;
	@Getter @Setter(AccessLevel.PROTECTED) private String merkleRoot;
	
	protected boolean isHashValid(String calculatedhash, int difficulty) {
		String validHash = StringUtils.repeat("0", difficulty);
		if ( calculatedhash.startsWith(validHash) ) 
			return true;
		return false;
	}

}
