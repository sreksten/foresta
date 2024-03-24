package com.threeamigos.foresta.motore;

public class Logger {
	
	private Logger() {
	}

	public static final void log(String messaggio) {
		System.out.println(messaggio);
	}
	
	public static final void log(Exception e) {
		e.printStackTrace();
	}
}
