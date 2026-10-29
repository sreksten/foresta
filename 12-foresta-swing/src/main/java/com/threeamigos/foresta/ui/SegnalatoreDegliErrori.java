package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.interni.InternoException;

import java.util.function.Consumer;

/**
 * Dice al giocatore che c'è stato un errore interno, invece di lasciarlo solo nel log: una transizione andata a metà può lasciare il
 * gioco fermo, e senza un messaggio non si capisce perché. Lo stesso errore, di fila, si dice una volta sola.
 */
final class SegnalatoreDegliErrori {

	private final Consumer<String> mostra;
	private String ultimoMostrato;

	/**
	 * @param mostra dove si scrive il messaggio per il giocatore
	 */
	SegnalatoreDegliErrori(Consumer<String> mostra) {
		this.mostra = mostra;
	}

	void segnala(InternoException evento) {
		String descrizione = descrizione(evento);
		if (descrizione.equals(ultimoMostrato)) {
			return;
		}
		ultimoMostrato = descrizione;
		mostra.accept("Errore interno: " + descrizione + ". Il gioco prosegue, ma qualcosa potrebbe non andare come previsto: se si blocca, salva se puoi e riavvia.");
	}

	private static String descrizione(InternoException evento) {
		String messaggio = evento.getMessaggio();
		if (messaggio != null && !messaggio.isEmpty()) {
			return messaggio;
		}
		Exception eccezione = evento.getException();
		return eccezione == null ? "errore sconosciuto" : eccezione.getClass().getSimpleName();
	}
}
