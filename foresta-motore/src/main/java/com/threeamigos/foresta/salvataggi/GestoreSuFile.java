package com.threeamigos.foresta.salvataggi;

import java.io.File;
import com.threeamigos.foresta.strumenti.Logger;

/**
 * La base dei gestori che scrivono i file del gioco (salvataggi, classifica, trofei) in un'unica cartella.
 * <p>
 * Chi li crea può indicare la cartella (su Android e iOS sta in uno spazio privato dell'app, che solo la piattaforma
 * conosce); senza, è la cartella {@code .foresta} della cartella dell'utente, come sul desktop.
 * <p>
 * I gestori leggono e scrivono con {@code java.io.File} e non con {@code java.nio.file}, che su Android chiede almeno
 * l'API 26: per sostituire un salvataggio con quello scritto in un file temporaneo vedi {@code GestoreSalvataggiSuFile}.
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

	private static File directoryNellaCartellaDellUtente() {
		String homeName = System.getProperty("user.home");
		File homeFile = new File(homeName);
		if (!homeFile.isDirectory()) {
			throw new IllegalArgumentException("user.home non e' una directory");
		}
		return new File(homeFile.getPath() + File.separatorChar + NOME_DIRECTORY);
	}
}
