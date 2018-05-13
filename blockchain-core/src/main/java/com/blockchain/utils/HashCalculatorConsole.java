package com.blockchain.utils;

import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;

public class HashCalculatorConsole {

	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		String hash1 = enterFromUser("Hash #1...:");
		String hash2 = enterFromUser("Hash #2...:");

		String result = DigestUtils.sha256Hex(hash1 + hash2);
		System.out.println("Result....:" + result);
		
		enterFromUser("Press... <ENTER>");
		
		System.out.print("------------------------->>> END");
		System.out.flush();
		
		scanner.close();

	}

	public static String enterFromUser(String prompt) {
		System.out.print(prompt);
		String readString = scanner.nextLine();
		return readString;
	}

}
