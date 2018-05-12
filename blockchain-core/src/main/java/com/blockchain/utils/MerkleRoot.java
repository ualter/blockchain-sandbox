package com.blockchain.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * This is a "utility" where we have an implementation of the Block's Merkle Root
 * This Hash is used to prove the integrity of all of the transactions in a Block, that they weren't changed, tampered. 
 * The value of this Merkle Root should always be the same if any of the original's values were not touched.
 * 
 * @author Ualter Junior
 */
public class MerkleRoot {

	public static String calculate(List<String> hashs) {
		String merkleRoot = null;
		if (hashs.size() == 1) {
			merkleRoot = DigestUtils.sha256Hex(hashs.get(0));
		} else {
			while (hashs.size() > 1) {
				hashs = calculateLeavesHash(hashs);
			}
			merkleRoot = hashs.get(0);
		}
		return merkleRoot;
	}

	private static List<String> calculateLeavesHash(List<String> hashs) {
		List<String> leavesHash = new ArrayList<String>();

		int index = 0;
		while (true) {

			String hash1 = hashs.get(index);

			index++;
			if (index < hashs.size()) {
				// Calculate the hash of the Pair
				String hash2 = hashs.get(index);
				String hashLeaf = DigestUtils.sha256Hex(hash1 + hash2);
				leavesHash.add(hashLeaf);
			} else if (index == hashs.size()) {
				// The last one, without a pair, the total List is odd
				// As the Merkle Tree is a binary tree, we duplicate the last register to calculate the leaf
				String hashLeaf = DigestUtils.sha256Hex(hash1 + hash1);
				leavesHash.add(hashLeaf);
			}

			index++;
			if (index >= hashs.size())
				break;
		}
		return leavesHash;
	}

}
