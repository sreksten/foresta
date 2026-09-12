package com.threeamigos.foresta.motore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static final PrintWriter fileWriter = apriFileDiLog();

	private Logger() {
	}

	/**
	 * Oltre alla console, teniamo traccia dei log anche su file: la console non è
	 * sempre visibile (es. lancio senza terminale), e serve poter consultare i log
	 * di una sessione anche dopo che il gioco è stato chiuso.
	 */
	private static PrintWriter apriFileDiLog() {
		try {
			File cartella = new File("logs");
			cartella.mkdirs();
			String nomeFile = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".log";
			return new PrintWriter(new FileWriter(new File(cartella, nomeFile), true), true);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	public static void log(String messaggio) {
		System.out.println(messaggio);
		if (fileWriter != null) {
			fileWriter.println(messaggio);
		}
	}

	public static void log(Exception e) {
		e.printStackTrace(System.err);
		if (fileWriter != null) {
			e.printStackTrace(fileWriter);
		}
	}
}
