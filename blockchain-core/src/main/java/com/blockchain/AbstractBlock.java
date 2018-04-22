package com.blockchain;

import org.apache.commons.lang.StringUtils;

import com.blockchain.utils.CryptoHashUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBlock {
	
	@Getter @Setter(AccessLevel.PUBLIC)    private Integer height;
	@Getter @Setter(AccessLevel.PROTECTED) private String  hash;
	@Getter @Setter(AccessLevel.PUBLIC)    private String  previousBlock;
	@Getter @Setter(AccessLevel.PUBLIC)    private String  nextBlock;
	@Getter @Setter(AccessLevel.PROTECTED) private Long    timeStamp;
	@Getter @Setter(AccessLevel.PROTECTED) private Integer nonce;
	@Getter @Setter(AccessLevel.PROTECTED) private String  merkleRoot;
	
	protected boolean isHashValid(String calculatedhash, int difficulty) {
		String validHash = StringUtils.repeat("0", difficulty);
		if ( calculatedhash.startsWith(validHash) ) 
			return true;
		return false;
	}
	
	protected void calculateHash(String data, int difficulty) {
		int    _nonce           = -1;
		String calculatedhash   = "";
		
		while ( !isHashValid(calculatedhash, difficulty) ) {
			calculatedhash = CryptoHashUtils.applySHA256(
					  data
					+ Long.toString(this.getTimeStamp())  
					+ this.getPreviousBlock() // connecting the blocks in the chain (the hash of this one is created using the hash of the previous one)
					+ (_nonce+=1)
					//+ this.getMerkleRoot()
			);
		}
		this.setNonce(_nonce);
		log.debug("Block: {}, data: {}, nonce: {}", calculatedhash, data, _nonce);
		this.setHash( calculatedhash );
	}

}
