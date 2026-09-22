package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Una notizia destinata alla schermata della mappa: un identificativo e un corpo
 * testuale. A differenza dei messaggi del pannello di testo, non è legata al
 * momento in cui viene mostrata al giocatore.
 *
 * @author Stefano Reksten
 */
public class Notizia implements Serializzabile {

	private String id;
	private String corpo;

	public Notizia() {
	}

	public Notizia(String id, String corpo) {
		this.id = id;
		this.corpo = corpo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCorpo() {
		return corpo;
	}

	public void setCorpo(String corpo) {
		this.corpo = corpo;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(id);
		stream.print(PIPE);
		stream.println(corpo);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		id = st.nextToken();
		corpo = st.nextToken();
	}
}
