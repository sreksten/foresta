package com.threeamigos.foresta.strumenti;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	/**
	 * La cartella dei file di log, se indicata con {@code -Dforesta.log=cartella}: senza, si scrive solo sulla console.
	 * Una sessione di gioco produce migliaia di righe, quindi conviene puntarla a un disco in RAM
	 * (vedi la nota su macOS in apriFileDiLog) per non consumare il disco.
	 */
	public static final String PROPRIETA_CARTELLA_DI_LOG = "foresta.log";

	private static final PrintWriter fileWriter = apriFileDiLog();

	private Logger() {
	}

	/**
	 * Oltre alla console, teniamo traccia dei log anche su file: la console non è
	 * sempre visibile (es. lancio senza terminale), e serve poter consultare i log
	 * di una sessione anche dopo che il gioco è stato chiuso.
	 * <p>
	 * Su macOS un disco in RAM da 128 MB si crea con
	 * {@code hdiutil attach -nomount ram://262144} e {@code diskutil erasevolume HFS+ ForestaLog /dev/diskN}
	 * (compare in /Volumes/ForestaLog); si toglie con {@code hdiutil detach /dev/diskN}.
	 */
	private static PrintWriter apriFileDiLog() {
		String cartellaDiLog = System.getProperty(PROPRIETA_CARTELLA_DI_LOG);
		if (cartellaDiLog == null || cartellaDiLog.isEmpty()) {
			return null;
		}
		try {
			File cartella = new File(cartellaDiLog);
			cartella.mkdirs();
			String nomeFile = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".log";
			return new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(new File(cartella, nomeFile), true), StandardCharsets.UTF_8), true);
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
