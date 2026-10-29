package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreTrofei;
import com.threeamigos.foresta.modellodati.TrofeiMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * I trofei vinti, nel file "trofei" della cartella .foresta.
 */
public final class GestoreTrofeiSuFile extends GestoreSuFile implements GestoreTrofei {

	private String nomeFile;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestoreTrofeiSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestoreTrofeiSuFile(File directory) {
		super(directory);
	}

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "trofei";
		return nomeFile;
	}

	/**
	 * Un file assente non è un errore: non si è ancora vinto nessun trofeo.
	 */
	public boolean carica(TrofeiMD trofeiMD) {
		File file = new File(nomeFile());
		if (!file.exists()) {
			return true;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			trofeiMD.leggi(reader);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			// Il file si mette da parte: la prossima scrittura non deve cancellare i trofei che c'erano
			mettiDaParteIlFileRovinato(file);
			return false;
		}
		return true;
	}

	/**
	 * Scrive i trofei nel file, passando da un file temporaneo; dice se ci è riuscito.
	 */
	public boolean salva(TrofeiMD trofeiMD) {
		try {
			scriviSostituendo(new File(nomeFile()), trofeiMD::salva);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
