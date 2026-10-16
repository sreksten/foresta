package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Il tempo della partita: giorno e ora correnti, le città distrutte, se il gioco è finito e l'ultimo evento
 * non ancora mostrato al giocatore (per esempio la colonna di fumo di una città in fiamme).
 *
 * @author Stefano Reksten
 */
public class LineaTemporaleMD implements Serializzabile {

	/**
	 * L'ora a cui parte la partita, e da cui ogni giornata riprende: le otto del mattino.
	 */
	public static final int PRIMA_ORA_DEL_MATTINO = 8;

	private int ora;
	private int giorno;
	private final List<TipoLocazione> cittaDistrutte = new ArrayList<>();
	private boolean giocoFinito;
	// Il messaggio dell'ultimo evento (es. la colonna di fumo), non ancora mostrato al giocatore
	private String evento;
	
	public int getOra() {
		return ora;
	}

	public void setOra(int ora) {
		this.ora = ora;
	}

	public int getGiorno() {
		return giorno;
	}

	public void setGiorno(int giorno) {
		this.giorno = giorno;
	}

	/**
	 * Le città distrutte, nell'ordine in cui lo sono state. Di sola lettura: si aggiunge con {@link #addCittaDistrutta}.
	 */
	public List<TipoLocazione> getCittaDistrutte() {
		return Collections.unmodifiableList(cittaDistrutte);
	}

	public boolean isGiocoFinito() {
		return giocoFinito;
	}

	public void setGiocoFinito(boolean giocoFinito) {
		this.giocoFinito = giocoFinito;
	}

	public String getEvento() {
		return evento;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	/////////////////////////

	/**
	 * Riporta il tempo all'inizio della partita: primo giorno, prima ora del mattino, nessuna città distrutta.
	 */
	public void reimposta() {
		ora = PRIMA_ORA_DEL_MATTINO;
		giorno = 1;
		giocoFinito = false;
		evento = null;
		cittaDistrutte.clear();
	}

	/**
	 * Registra che quella città è stata distrutta.
	 */
	public void addCittaDistrutta(TipoLocazione citta) {
		cittaDistrutte.add(citta);
	}

	/**
	 * Dice se quella città è stata distrutta.
	 */
	public boolean isCittaDistrutta(TipoLocazione citta) {
		return cittaDistrutte.contains(citta);
	}

	/**
	 * Scrive su una riga ora, giorno, se il gioco è finito, l'ultimo evento (anche vuoto) e poi, se ce ne sono, le città
	 * distrutte, tutto separato da {@link #PIPE}.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(ora);
		stream.print(PIPE);
		stream.print(giorno);
		stream.print(PIPE);
		stream.print(giocoFinito);
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(Serializzabile.pulisciCampo(evento)));
		for (TipoLocazione cittaDistrutta : cittaDistrutte) {
			stream.print(PIPE);
			stream.print(cittaDistrutta.name());
		}
		stream.println("");
	}

	/**
	 * Rilegge la riga scritta da {@link #salva(PrintWriter)}: dopo i primi quattro campi, tutti gli altri sono città distrutte.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		ora = Integer.parseInt(st.testo());
		giorno = Integer.parseInt(st.testo());
		giocoFinito = st.booleano();
		evento = st.testoFacoltativo();
		cittaDistrutte.clear();
		while (st.haAltriCampi()) {
			cittaDistrutte.add(st.enumerato(TipoLocazione.class));
		}
	}
}
