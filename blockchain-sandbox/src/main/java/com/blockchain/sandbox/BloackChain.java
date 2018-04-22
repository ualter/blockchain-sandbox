package com.blockchain.sandbox;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BloackChain {

	public static List<Block> blockchain = new ArrayList<Block>();
	
	public static int difficulty = 3;

	public static void main(String[] args) throws Exception {

		blockchain.add(new Block("Hi im the first block", "0"));
		blockchain.get(0).mineBlock(difficulty);
		blockchain.add(new Block("Yo im the second block", blockchain.get(blockchain.size() - 1).getHash()));
		blockchain.get(1).mineBlock(difficulty);
		blockchain.add(new Block("Hey im the third block", blockchain.get(blockchain.size() - 1).getHash()));
		blockchain.get(2).mineBlock(difficulty);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonBlockchain = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(blockchain);

		System.out.println(isBlockChainValid());
		
		System.out.println(jsonBlockchain);
		

	}

	public static boolean isBlockChainValid() {

		Block currentBlock;
		Block previousBlock;

		for (int i = 1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i - 1);
			
			// compare registered hash and calculated hash:
			if ( !currentBlock.getHash().equals( currentBlock.calculateHash() )) {
				System.out.println("Current Hashes not equal");
				return false;
			}
			
			// compare previous hash and registered previous hash
			if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
		}

		return true;

	}
}