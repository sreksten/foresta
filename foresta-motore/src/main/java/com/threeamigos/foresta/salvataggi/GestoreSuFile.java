package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.strumenti.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * La base dei gestori che scrivono i file del gioco (salvataggi, classifica, trofei) in un'unica cartella.
 * <p>
 * Chi li crea può indicare la cartella (su Android e iOS sta in uno spazio privato dell'app, che solo la piattaforma
 * conosce); senza, è la cartella {@code .foresta} della cartella dell'utente, come sul desktop.
 * <p>
 * I gestori leggono e scrivono con {@code java.io.File} e non con {@code java.nio.file}, che su Android chiede almeno
 * l'API 26: per sostituire un file con quello scritto in un file temporaneo vedi {@link #scriviSostituendo}.
 */
public abstract class GestoreSuFile {

	protected static final String NOME_DIRECTORY = ".foresta";

	// Null: la cartella .foresta della cartella dell'utente
	private final File directoryIndicata;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	protected GestoreSuFile() {
		this(null);
	}

	/**
	 * @param directory la cartella dove stanno i file del gioco, creata se non esiste; con null, la cartella
	 *                  {@code .foresta} della cartella dell'utente
	 */
	protected GestoreSuFile(File directory) {
		this.directoryIndicata = directory;
	}

	/**
	 * La cartella dei file del gioco, creata se non esiste.
	 *
	 * @throws IllegalArgumentException se la cartella dell'utente non è una directory
	 */
	protected File recuperaDirectory() {
		File directory = directoryIndicata != null ? directoryIndicata : directoryNellaCartellaDellUtente();
		Logger.log(() -> "Directory: " + directory.getPath());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	/**
	 * Quello che si scrive in un file, riga per riga.
	 */
	protected interface Scrittura {
		void scrivi(PrintWriter writer) throws IOException;
	}

	/**
	 * Scrive prima su un file temporaneo (in UTF-8) e lo sostituisce alla destinazione solo se è stato scritto per intero: un errore a
	 * metà lascia intatto il file che c'era.
	 *
	 * @throws IOException se la scrittura o la sostituzione non riescono
	 */
	protected static void scriviSostituendo(File destinazione, Scrittura scrittura) throws IOException {
		File temporaneo = new File(destinazione.getPath() + ".tmp");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(temporaneo), StandardCharsets.UTF_8)))) {
			scrittura.scrivi(writer);
			// PrintWriter non lancia eccezioni sugli errori di scrittura: vanno chiesti
			if (writer.checkError()) {
				throw new IOException("Errore di scrittura di " + destinazione.getPath());
			}
		} catch (IOException | RuntimeException e) {
			temporaneo.delete();
			throw e;
		}
		try {
			sostituisci(temporaneo, destinazione);
		} catch (IOException e) {
			temporaneo.delete();
			throw e;
		}
	}

	/**
	 * Mette il file temporaneo al posto della destinazione. Dove {@link File#renameTo(File)} sostituisce un file che c'è già (macOS,
	 * Linux, Android, iOS) lo fa in un colpo solo; dove no (Windows) si toglie prima il vecchio file, e un errore a metà lascia comunque
	 * il file nuovo nel temporaneo.
	 */
	protected static void sostituisci(File temporaneo, File destinazione) throws IOException {
		if (temporaneo.renameTo(destinazione)) {
			return;
		}
		if (destinazione.exists() && !destinazione.delete()) {
			throw new IOException("Impossibile sostituire " + destinazione.getPath());
		}
		if (!temporaneo.renameTo(destinazione)) {
			throw new IOException("Impossibile spostare " + temporaneo.getPath() + " in " + destinazione.getPath());
		}
	}

	/**
	 * Un file che non si riesce a leggere si mette da parte (con l'estensione {@code .rovinato}) invece di lasciarlo dov'è: la prossima
	 * scrittura lo sostituirebbe con quello che c'è in memoria, e quello che conteneva di buono andrebbe perso per sempre.
	 */
	protected static void mettiDaParteIlFileRovinato(File file) {
		File daParte = new File(file.getPath() + ".rovinato");
		if (daParte.exists()) {
			daParte.delete();
		}
		if (!file.renameTo(daParte)) {
			Logger.log(() -> "Impossibile mettere da parte il file rovinato " + file.getPath());
		}
	}

	private static File directoryNellaCartellaDellUtente() {
		String homeName = System.getProperty("user.home");
		File homeFile = new File(homeName);
		if (!homeFile.isDirectory()) {
			throw new IllegalArgumentException("user.home non e' una directory");
		}
		return new File(homeFile.getPath() + File.separatorChar + NOME_DIRECTORY);
	}
}
