package com.blockchain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.blockchain.utils.CryptoHashUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Ualter Junior
 *
 */
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
	
	protected void startMining(String data, int difficulty) {
		if ( this.getHash() != null ) {
			throw new RuntimeException("The Hash of this Block were already calculated");
		}
		
		int    _nonce           = -1;
		String calculatedhash   = "";
		
		while ( !isHashValid(calculatedhash, difficulty) ) {
			calculatedhash = CryptoHashUtils.applySHA256(
					  data
					+ Long.toString(this.getTimeStamp())  
					+ this.getPreviousBlock() // connecting the blocks in the chain (the hash of this one is created using the hash of the previous one)
					+ (_nonce+=1)
			);
		}
		this.setNonce(_nonce);
		this.setHash( calculatedhash );
	}
	
	/**
	 * This where is calculated the Block's Merkle Root
	 * This Hash is used to prove the integrity of all of the transactions in this Block, that they weren't changed, tampered. 
	 * The value os this Merkle Root should always be the same if any of the original's values were not touched.
	 */
	protected void calculateMerkleRoot(List<String> hashs) {
		if ( this.getMerkleRoot() != null ) {
			throw new RuntimeException("The Merkle Root of this Block were already calculated");
		}
		
		String merkleRoot = null;
		
		if (hashs.size() == 1) {
			// Only one register, then there is nothing more to do, the only register's hash is also the Merkle Root
			merkleRoot =  CryptoHashUtils.applySHA256( hashs.get(0) );
		} else {
			while ( hashs.size() > 1 ) {
				hashs = calculateLeavesHash(hashs);
			}
			merkleRoot = hashs.get(0);
		}
		this.setMerkleRoot(merkleRoot);
	}
	
	

	private List<String> calculateLeavesHash(List<String> hashs) {
		List<String> leavesHash = new ArrayList<String>();
		
		int index = 0;
		while ( true ) {
			
			String hash1 = hashs.get(index);
			
			index++;
			if ( index < hashs.size() ) {
				// Calculate the hash of the Pair
				String hash2    = hashs.get(index);
				String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash2 );
				leavesHash.add(hashLeaf);
			} else
			if ( index == hashs.size() ) {
				// The last one, without a pair, the total List is odd
				// As the Merkle Tree is a binary tree, we duplicate the last register to calculate the leaf
				String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash1 );
				leavesHash.add(hashLeaf);
			}
			
			index++;
			if ( index >= hashs.size() ) break;
		}
		
		return leavesHash;
	}

}
