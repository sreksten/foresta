package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.modellodati.BestiarioMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Il bestiario, nel file "bestiario" della cartella .foresta.
 */
public final class GestoreBestiarioSuFile extends GestoreSuFile implements GestoreBestiario {

	private String nomeFile;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestoreBestiarioSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestoreBestiarioSuFile(File directory) {
		super(directory);
	}

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "bestiario";
		return nomeFile;
	}

	/**
	 * Un file assente non è un errore: non si è ancora sbloccata nessuna voce.
	 */
	public boolean carica(BestiarioMD bestiarioMD) {
		File file = new File(nomeFile());
		if (!file.exists()) {
			return true;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			bestiarioMD.leggi(reader);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			// Il file si mette da parte: la prossima scrittura non deve cancellare le voci che c'erano
			mettiDaParteIlFileRovinato(file);
			return false;
		}
		return true;
	}

	/**
	 * Scrive il bestiario nel file, passando da un file temporaneo; dice se ci è riuscito.
	 */
	public boolean salva(BestiarioMD bestiarioMD) {
		try {
			scriviSostituendo(new File(nomeFile()), bestiarioMD::salva);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
