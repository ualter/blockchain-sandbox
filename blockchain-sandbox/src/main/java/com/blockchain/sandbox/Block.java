package com.blockchain.sandbox;

import java.time.Instant;

import lombok.Data;

@Data
public class Block {

	private String hash;
	private String previousHash;
	private String data;
	private long timeStamp;
	private int nonce;

	public Block(String data, String previousHash) {
		this.previousHash = previousHash;
		this.data = data;
		this.timeStamp = Instant.now().toEpochMilli();
		this.hash = calculateHash();
	}

	public String calculateHash() {
		String calculatedhash = Util
				.applySHA256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + data);
		return calculatedhash;
	}

	public void mineBlock(int difficulty) {
		String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with difficulty * "0"
		while (!hash.substring(0, difficulty).equals(target)) {
			// System.out.println(target + " = " + hash.substring(0, difficulty) + "\t" +
			// hash);
			this.nonce++;
			this.hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
		System.out.println("\n\n\n");
	}

}
