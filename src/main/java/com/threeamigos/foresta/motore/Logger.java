package com.threeamigos.foresta.motore;

public class Logger {
	
	private Logger() {
	}

	public static void log(String messaggio) {
		System.out.println(messaggio);
	}
	
	public static void log(Exception e) {
		e.printStackTrace(System.err);
	}
}
