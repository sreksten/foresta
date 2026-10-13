package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreTrofei;
import com.threeamigos.foresta.modellodati.TrofeiMD;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * I trofei vinti, nel file "trofei" della cartella .foresta.
 */
public final class GestoreTrofeiSuFile extends GestoreSuFile implements GestoreTrofei {

	private String nomeFile;

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "trofei";
		return nomeFile;
	}

	/**
	 * Un file assente non è un errore: non si è ancora vinto nessun trofeo.
	 */
	public boolean carica(TrofeiMD trofeiMD) {
		Path file = Paths.get(nomeFile());
		if (!Files.exists(file)) {
			return true;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
			trofeiMD.leggi(reader);
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}

	public boolean salva(TrofeiMD trofeiMD) {
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(nomeFile())), StandardCharsets.UTF_8))) {
			trofeiMD.salva(writer);
			writer.flush();
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
