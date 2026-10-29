package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static com.threeamigos.foresta.serializzazione.Serializzabile.*;

/**
 * Il tempo della partita: giorno e ora correnti, se il gioco è finito e l'ultimo evento
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
	// Vale solo durante la partita e non si salva: quando diventa vero la partita finisce subito (vinta o persa), senza un
	// momento in cui la si possa salvare
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
	 * Riporta il tempo all'inizio della partita: primo giorno, prima ora del mattino.
	 */
	public void reimposta() {
		ora = PRIMA_ORA_DEL_MATTINO;
		giorno = 1;
		giocoFinito = false;
		evento = null;
	}

	/**
	 * Scrive su una riga ora, giorno e l'ultimo evento (anche vuoto), separati da {@link #PIPE}. Se il gioco è finito non si scrive: una partita finita non si salva.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream,
				ora,
				giorno,
				facoltativo(pulisciCampo(evento)));
	}

	/**
	 * Rilegge la riga scritta da {@link #salva(PrintWriter)}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi st = new LettoreCampi(stream.readLine());
		ora = st.intero();
		giorno = st.intero();
		giocoFinito = false;
		evento = st.testoFacoltativo();
	}

}
