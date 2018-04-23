package com.blockchain;

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
	
	protected void calculateHash(String data, int difficulty) {
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
		log.debug("Block: {}, data: {}, nonce: {}", calculatedhash, data, _nonce);
		this.setHash( calculatedhash );
	}
	
	/**
	 * This where is calculated the Block's Merkle Root
	 * This Hash is used to prove the integrity of all of the transactions in this Block, that they weren't changed, tampered. 
	 * The value os this Merkle Root should always be the same if any of the original's values were touched.
	 */
	protected void calculateMerkleRoot(List<String> hashs) {
		if ( this.getMerkleRoot() != null ) {
			throw new RuntimeException("The Merkle Root of this Block were already calculated");
		}
		
		String merkleRoot = null;
		
		// Only one register, then there is nothing more to do, the only register's hash is also the Merkle Root
		if ( hashs.size() == 1 ) {
			merkleRoot =  CryptoHashUtils.applySHA256( hashs.get(0) );
		} else {
			// More than one register, calculate the Merkle Root based on all of them 
			String previousHashLeaf = null;
			while ( true ) {
				
				String hash1 = hashs.get(0);
				// Still has pairs at the List
				if ( hashs.size() > 1 ) {
					// Calculate the hash of the Pair
					String hash2    = hashs.get(1);
					String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash2 );
					// Remove the pair of the leave with the calculated hash
					hashs.remove(0);
					hashs.remove(0);
	
					if ( previousHashLeaf != null ) {
						// Subsequent leaves must consider the previous leaf already calculated
						previousHashLeaf = CryptoHashUtils.applySHA256( previousHashLeaf + hashLeaf );
					} else {
						// First leaf on the tree
						previousHashLeaf = hashLeaf;
					}
				} else {
				// The last one, without a pair, the total List is odd
					// As the Merkle Tree is a binary tree, we duplicate the last register to calculate the leaf
					String hashLeaf  = CryptoHashUtils.applySHA256( hash1 + hash1 );
					previousHashLeaf = CryptoHashUtils.applySHA256( previousHashLeaf + hashLeaf );
					hashs.remove(0);
				}
				
				if ( hashs.size() == 0 ) break;
			}
			
			merkleRoot = previousHashLeaf;
			log.debug("Block Merkle Root calculated: {}", merkleRoot);
			this.setMerkleRoot(merkleRoot);
		}
	}

}
