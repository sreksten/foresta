package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoSuggerimento;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Il tutorial: se è acceso e quali suggerimenti sono già stati visti. Non fa parte di {@code ModelloDati}: non appartiene a una partita ma passa
 * dall'una all'altra, quindi non viene mai reimpostato e si salva in un file a parte, come il bestiario.
 * <p>
 * Formato: una prima riga con lo stato (acceso o spento), poi una riga per ogni suggerimento visto.
 *
 * @author Stefano Reksten
 */
public class SuggerimentiMD implements Serializzabile {

	private boolean attivo = true;
	private final Set<TipoSuggerimento> visti = EnumSet.noneOf(TipoSuggerimento.class);

	public boolean isAttivo() {
		return attivo;
	}

	public void setAttivo(boolean attivo) {
		this.attivo = attivo;
	}

	public boolean isVisto(TipoSuggerimento suggerimento) {
		return visti.contains(suggerimento);
	}

	/**
	 * Segna come visto un suggerimento.
	 *
	 * @return true se non lo era già
	 */
	public boolean segnaVisto(TipoSuggerimento suggerimento) {
		return visti.add(suggerimento);
	}

	/**
	 * I suggerimenti visti, nell'ordine dell'enum.
	 */
	public Set<TipoSuggerimento> getVisti() {
		return Collections.unmodifiableSet(visti);
	}

	/**
	 * Ripristina il tutorial: torna acceso e non ha visto niente.
	 */
	public void ripristina() {
		attivo = true;
		visti.clear();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream, attivo);
		for (TipoSuggerimento suggerimento : visti) {
			stampaRiga(stream, suggerimento);
		}
	}

	/**
	 * Un suggerimento che il gioco non ha più (una costante rinominata o tolta) si salta: non deve impedire di leggere gli altri. Una prima riga
	 * che non è lo stato (true o false) è un file rovinato: lancia IOException.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		ripristina();
		String riga = stream.readLine();
		if (riga == null || riga.isEmpty()) {
			return;
		}
		// Una prima riga che non dice né true né false è un file rovinato (booleano() la leggerebbe come falso e spegnerebbe il tutorial)
		String stato = new LettoreCampi(riga).testo();
		if (!"true".equals(stato) && !"false".equals(stato)) {
			throw new IOException("Il file del tutorial non comincia con lo stato (true o false): " + stato);
		}
		attivo = Boolean.parseBoolean(stato);
		while ((riga = stream.readLine()) != null) {
			if (riga.isEmpty()) {
				continue;
			}
			try {
				visti.add(TipoSuggerimento.valueOf(new LettoreCampi(riga).testo()));
			} catch (IllegalArgumentException e) {
				// Un suggerimento che non esiste più non rovina il caricamento
			}
		}
	}
}
