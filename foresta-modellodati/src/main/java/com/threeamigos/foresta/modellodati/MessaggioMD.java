package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import static com.threeamigos.foresta.modellodati.Serializzabile.facoltativo;
import static com.threeamigos.foresta.modellodati.Serializzabile.senzaACapo;
import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

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

	/**
	 * L'identificativo unico del messaggio.
	 */
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
	 * anche "|"; gli a capo diventano spazi. Un testo che manca si scrive come campo vuoto, e si rilegge come testo vuoto.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream,
				uuid,
				paragrafo ? PARAGRAFO : FRASE,
				facoltativo(senzaACapo(testo)));
	}

	/**
	 * Rilegge la riga scritta da {@link #salva(PrintWriter)}; un flusso finito troppo presto lancia un'{@link IOException}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		uuid = campi.testo();
		String tipo = campi.testo();
		if (tipo.length() != 1 || (tipo.charAt(0) != PARAGRAFO && tipo.charAt(0) != FRASE)) {
			throw new IOException("Tipo di messaggio non valido: " + tipo);
		}
		paragrafo = tipo.charAt(0) == PARAGRAFO;
		// Il testo è l'ultimo campo e puo' contenere il "|"
		testo = campi.resto();
	}
}
