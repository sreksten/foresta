package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Un messaggio mostrato al giocatore nel pannello di testo, con il modo in cui è
 * stato mostrato: come inizio di un nuovo paragrafo (NotificaTestoParagrafo) o come
 * continuazione del precedente (NotificaTestoFrase). Serve a ripopolare il pannello
 * dopo un ricaricamento con la stessa impaginazione.
 *
 * @author Stefano Reksten
 */
public class MessaggioMD implements Serializzabile {

	private static final char PARAGRAFO = 'P';
	private static final char FRASE = 'F';

	/**
	 * Un identificativo unico per il messaggio
	 */
	private String uuid = UUID.randomUUID().toString();

	private String testo;
	private boolean paragrafo;

	public MessaggioMD() {
	}

	public MessaggioMD(String testo, boolean paragrafo) {
		this.testo = testo;
		this.paragrafo = paragrafo;
	}

	public String getUuid() {
		return uuid;
	}

	public String getTesto() {
		return testo;
	}

	/**
	 * @return true se il messaggio apriva un nuovo paragrafo, false se continuava il precedente
	 */
	public boolean isParagrafo() {
		return paragrafo;
	}

	/**
	 * Una riga per messaggio: uuid, il tipo (P o F) e il testo, ciascuno separato da "|".
	 * Il testo viene riletto per intero dopo il secondo separatore, quindi può contenere
	 * anche "|".
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(uuid);
		stream.print(PIPE);
		stream.print(paragrafo ? PARAGRAFO : FRASE);
		stream.print(PIPE);
		stream.println(testo);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		if (line == null) {
			throw new IOException("Messaggio mancante");
		}
		int primoPipe = line.indexOf(PIPE);
		int secondoPipe = line.indexOf(PIPE, primoPipe + 1);
		uuid = line.substring(0, primoPipe);
		paragrafo = line.charAt(primoPipe + 1) == PARAGRAFO;
		testo = line.substring(secondoPipe + 1);
	}
}
