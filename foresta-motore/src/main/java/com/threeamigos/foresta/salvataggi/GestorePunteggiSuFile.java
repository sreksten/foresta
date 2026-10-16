package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import com.threeamigos.foresta.modellodati.Serializzabile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import com.threeamigos.foresta.strumenti.Punteggio;

/**
 * La classifica nel file {@code forestaHS} della cartella {@code .foresta}.
 */
public final class GestorePunteggiSuFile extends GestorePunteggiBase {

	private String nomeFile;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestorePunteggiSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestorePunteggiSuFile(File directory) {
		super(directory);
	}

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "forestaHS";
		return nomeFile;
	}

	/**
	 * Legge la classifica dal file, una riga per punteggio; dice se ci è riuscito.
	 */
	public boolean carica() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nomeFile()), StandardCharsets.UTF_8))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				LettoreCampi campi = new LettoreCampi(reader.readLine());
				String idPartita = campi.testoFacoltativo();
				String nome = campi.testo();
				int punteggio = campi.intero();
				setPunteggio(posizione, nome, punteggio, idPartita);
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}

	/**
	 * Scrive la classifica nel file, una riga per punteggio; dice se ci è riuscito.
	 */
	public boolean salva() {
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nomeFile()), StandardCharsets.UTF_8))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				Punteggio punteggio = getPunteggio(posizione);
				writer.print(Serializzabile.facoltativo(punteggio.getIdPartita()));
				writer.print(Serializzabile.PIPE);
				writer.print(punteggio.getNome());
				writer.print(Serializzabile.PIPE);
				writer.println(punteggio.getPunteggio());
			}
			writer.flush();
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
