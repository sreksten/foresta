package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreSuggerimenti;
import com.threeamigos.foresta.modellodati.SuggerimentiMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Il tutorial (se è acceso e quali suggerimenti sono già stati visti), nel file "tutorial" della cartella .foresta: vale da una partita all'altra,
 * come il bestiario.
 */
public final class GestoreSuggerimentiSuFile extends GestoreSuFile implements GestoreSuggerimenti {

	private String nomeFile;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestoreSuggerimentiSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestoreSuggerimentiSuFile(File directory) {
		super(directory);
	}

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "tutorial";
		return nomeFile;
	}

	/**
	 * Un file assente non è un errore: il tutorial non è ancora stato visto, ed è acceso.
	 */
	public boolean carica(SuggerimentiMD suggerimentiMD) {
		File file = new File(nomeFile());
		if (!file.exists()) {
			return true;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			suggerimentiMD.leggi(reader);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			// Il file si mette da parte: la prossima scrittura non deve cancellare i suggerimenti che si erano visti
			mettiDaParteIlFileRovinato(file);
			return false;
		}
		return true;
	}

	/**
	 * Scrive il tutorial nel file, passando da un file temporaneo; dice se ci è riuscito.
	 */
	public boolean salva(SuggerimentiMD suggerimentiMD) {
		try {
			scriviSostituendo(new File(nomeFile()), suggerimentiMD::salva);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
