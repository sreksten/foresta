package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * Una notizia destinata alla schermata della mappa: un identificativo e un corpo
 * testuale. A differenza dei messaggi del pannello di testo, non è legata al
 * momento in cui viene mostrata al giocatore.
 *
 * @author Stefano Reksten
 */
public class NotiziaMD implements Serializzabile {

	private String id;
	private String corpo;

	public NotiziaMD() {
	}

	public NotiziaMD(String id, String corpo) {
		this.id = Serializzabile.pulisciCampo(id);
		this.corpo = Serializzabile.pulisciCampo(corpo);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = Serializzabile.pulisciCampo(id);
	}

	public String getCorpo() {
		return corpo;
	}

	public void setCorpo(String corpo) {
		this.corpo = Serializzabile.pulisciCampo(corpo);
	}

	/**
	 * Scrive una riga con l'identificativo e il corpo, separati da {@link #PIPE}.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream,
				id,
				corpo);
	}

	/**
	 * Rilegge la riga scritta da {@link #salva(PrintWriter)}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi st = new LettoreCampi(stream.readLine());
		id = st.testo();
		corpo = st.testo();
	}
}
