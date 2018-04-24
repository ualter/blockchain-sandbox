package com.blockchain.cryptocurrency.pavo.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockchain.utils.CryptoHashUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DeleteLater {
	
	public static void main(String[] args) throws Exception {
		//testLambaPeek();
		//testMerkleRoot();
		
		
		Path pathFile = Paths.get("src/main/resources/jane.keys");
		if ( !Files.exists(pathFile) ) {
			CryptoHashUtils.saveKeyECSDAPairsInFile(pathFile.toAbsolutePath().toString());
		}
		
		CryptoHashUtils.loadKeyECSDAPairsInFile(pathFile.toAbsolutePath().toString());
	}
	
	private static void testMerkleRoot() {
		List<String> list = new ArrayList<String>();
		list.add("A");
		list.add("B");
		list.add("C");
		list.add("D");
		list.add("E");
		list.add("F");
		list.add("G");
		list.add("H");
		list.add("I");
		list.add("J");
		list.add("K");
		list.add("L");
		list.add("M");
		list.add("N");
		list.add("O");
		list.add("P");
		
		while ( list.size() > 1 ) {
			list = calculateLeavesHash(list);
		}
		System.out.println(list.get(0));
	}
	private static void testLambaPeek() {
		Map<String,Tra> m = new HashMap<String,Tra>();
		
		m.put("1", new Tra("@",20));
		m.put("2", new Tra("@",2));
		m.put("3", new Tra("@",1));
		m.put("4", new Tra("#",1));
		m.put("5", new Tra("#",1));
		m.put("6", new Tra("@",20));
		
		System.out.println(m);
		
		List<Tra> l = new ArrayList<Tra>();
		
		System.out.println(
		m.entrySet()
		.stream()
		.filter(e ->  e.getValue().getId().equals("@") )
		.peek(c -> l.add(c.getValue()))
		.mapToInt(e -> e.getValue().getValor() )
		.sum()
				);
		System.out.println(l);
	}
	
	@Data
	@AllArgsConstructor
	public static class Tra {
		
		private String id;
		private Integer valor;
	}
	
	
	private static List<String> calculateLeavesHash(List<String> hashs) {
		List<String> leavesHash = new ArrayList<String>();
		
		int index = 0;
		while ( true ) {
			
			String hash1 = hashs.get(index);
			
			index++;
			if ( index < hashs.size() ) {
				// Calculate the hash of the Pair
				String hash2    = hashs.get(index);
				//String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash2 );
				String hashLeaf = hash1 + hash2;
				leavesHash.add(hashLeaf);
			} else
			if ( index == hashs.size() ) {
				// The last one, without a pair, the total List is odd
				// As the Merkle Tree is a binary tree, we duplicate the last register to calculate the leaf
				//String hashLeaf = CryptoHashUtils.applySHA256( hash1 + hash1 );
				String hashLeaf = hash1 + hash1;
				leavesHash.add(hashLeaf);
			}
			
			index++;
			if ( index >= hashs.size() ) break;
		}
		
		return leavesHash;
	}

}

