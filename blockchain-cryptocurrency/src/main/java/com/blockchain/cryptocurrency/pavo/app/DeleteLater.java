package com.blockchain.cryptocurrency.pavo.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DeleteLater {
	
	public static void main(String[] args) {
		
		testLambaPeek();
		
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

}

