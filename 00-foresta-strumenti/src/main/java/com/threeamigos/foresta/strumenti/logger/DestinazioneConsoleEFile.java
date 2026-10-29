package com.threeamigos.foresta.strumenti.logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * La destinazione di log del desktop: scrive sulla console e, se si indica una cartella con
 * {@code -D}{@value Logger#PROPRIETA_CARTELLA_DI_LOG}{@code =cartella}, anche su un file per ogni sessione.
 */
public final class DestinazioneConsoleEFile implements DestinazioneDiLog {

	/**
	 * Oltre alla console, teniamo traccia dei log anche su file: la console non è
	 * sempre visibile (es. lancio senza terminale), e serve poter consultare i log
	 * di una sessione anche dopo che il gioco è stato chiuso.
	 * <p>
	 * Su macOS un disco in RAM da 128 MB si crea con
	 * {@code hdiutil attach -nomount ram://262144} e {@code diskutil erasevolume HFS+ ForestaLog /dev/diskN}
	 * (compare in /Volumes/ForestaLog); si toglie con {@code hdiutil detach /dev/diskN}.
	 */
	private static PrintWriter apriFileDiLog(String cartellaDiLog) {
		if (cartellaDiLog == null || cartellaDiLog.isEmpty()) {
			return null;
		}
		try {
			File cartella = new File(cartellaDiLog);
			cartella.mkdirs();
			// Locale.ROOT: il nome del file non deve cambiare con la lingua del telefono (calendario e cifre di un'altra lingua)
			String nomeFile = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".log";
			return new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(new File(cartella, nomeFile), true), StandardCharsets.UTF_8), true);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	// Null se non si scrive su file
	private final PrintWriter fileWriter;

	/**
	 * Legge la cartella di log dalla proprietà di sistema {@value Logger#PROPRIETA_CARTELLA_DI_LOG}.
	 */
	public DestinazioneConsoleEFile() {
		this(System.getProperty(Logger.PROPRIETA_CARTELLA_DI_LOG));
	}

	/**
	 * @param cartellaDiLog la cartella dei file di log; null o vuota per scrivere solo sulla console
	 */
	public DestinazioneConsoleEFile(String cartellaDiLog) {
		this.fileWriter = apriFileDiLog(cartellaDiLog);
	}

	@Override
	public void scrivi(String messaggio) {
		System.out.println(messaggio);
		if (fileWriter != null) {
			fileWriter.println(messaggio);
		}
	}

	@Override
	public void scrivi(Exception e) {
		e.printStackTrace(System.err);
		if (fileWriter != null) {
			e.printStackTrace(fileWriter);
		}
	}

}
