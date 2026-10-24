package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.serializzazione.LettoreCampi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.threeamigos.foresta.strumenti.Punteggio;

import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;
import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

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
		File file = new File(nomeFile());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				LettoreCampi campi = new LettoreCampi(reader.readLine());
				String idPartita = campi.testoFacoltativo();
				String nome = campi.testo();
				int punteggio = campi.intero();
				setPunteggio(posizione, nome, punteggio, idPartita);
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			// Il file si mette da parte (se c'è): la prossima scrittura non deve cancellare la classifica che c'era
			if (file.exists()) {
				mettiDaParteIlFileRovinato(file);
			}
			return false;
		}
		return true;
	}

	/**
	 * Scrive la classifica nel file, una riga per punteggio, passando da un file temporaneo; dice se ci è riuscito.
	 */
	public boolean salva() {
		try {
			scriviSostituendo(new File(nomeFile()), writer -> {
				for (int posizione = 0; posizione < getConteggio(); posizione++) {
					Punteggio punteggio = getPunteggio(posizione);
					stampaRiga(writer,
							facoltativo(punteggio.getIdPartita()),
							punteggio.getNome(),
							punteggio.getPunteggio());
				}
			});
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
