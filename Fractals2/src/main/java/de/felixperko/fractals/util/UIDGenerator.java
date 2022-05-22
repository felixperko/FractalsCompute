package de.felixperko.fractals.util;

import java.util.Base64;
import java.util.Random;

public class UIDGenerator {

	static Random rand = new Random();
	
	public static void main(String[] args) {
		
		for (int i = 0 ; i < 100 ; i++)
			System.out.println(fromRandomBytes(6));
	}
	
	public static String fromRandomBytes(int size) {
		byte[] bytes = new byte[size];
		rand.nextBytes(bytes);
		String str = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return str.substring(0, size);
	}
}
