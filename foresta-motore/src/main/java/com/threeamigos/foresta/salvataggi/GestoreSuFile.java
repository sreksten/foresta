package com.threeamigos.foresta.salvataggi;

import java.io.File;
import com.threeamigos.foresta.strumenti.Logger;

/**
 * La base dei gestori che scrivono nella cartella {@code .foresta} della cartella dell'utente (salvataggi, classifica, trofei).
 */
public abstract class GestoreSuFile {

	protected static final String NOME_DIRECTORY = ".foresta";

	/**
	 * La cartella dei file del gioco, creata se non esiste.
	 *
	 * @throws IllegalArgumentException se la cartella dell'utente non è una directory
	 */
	protected File recuperaDirectory() {
		String homeName = System.getProperty("user.home");
		File homeFile = new File(homeName);
		if (!homeFile.isDirectory()) {
			throw new IllegalArgumentException("user.home non e' una directory");
		}
		File directory = new File(homeFile.getPath() + File.separatorChar + NOME_DIRECTORY);
		Logger.log("Directory: " + directory.getPath());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}
}
