package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Un messaggio mostrato al giocatore nel pannello di testo, con il modo in cui è
 * stato mostrato: come inizio di un nuovo paragrafo (NotificaTestoParagrafo) o come
 * continuazione del precedente (NotificaTestoFrase). Serve a ripopolare il pannello
 * dopo un ricaricamento con la stessa impaginazione.
 *
 * @author Stefano Reksten
 */
public class Messaggio implements Serializzabile {

	private static final char PARAGRAFO = 'P';
	private static final char FRASE = 'F';

	private String testo;
	private boolean paragrafo;

	public Messaggio() {
	}

	public Messaggio(String testo, boolean paragrafo) {
		this.testo = testo;
		this.paragrafo = paragrafo;
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
	 * Una riga per messaggio: il tipo (P o F), il separatore e il testo. Il testo
	 * viene riletto per intero dopo il separatore, quindi può contenere anche "|".
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
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
		if (line.length() >= 2 && (line.charAt(0) == PARAGRAFO || line.charAt(0) == FRASE)
				&& line.substring(1, 2).equals(PIPE)) {
			paragrafo = line.charAt(0) == PARAGRAFO;
			testo = line.substring(2);
		} else {
			// Salvataggi precedenti all'introduzione del tipo: solo il testo, senza
			// sapere se fosse un paragrafo. Lo si tratta come continuazione.
			paragrafo = false;
			testo = line;
		}
	}
}
