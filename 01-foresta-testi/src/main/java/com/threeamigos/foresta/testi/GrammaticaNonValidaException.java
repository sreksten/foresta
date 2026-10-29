package com.threeamigos.foresta.testi;

/**
 * Una grammatica del gioco manca o non è valida: il messaggio dice quale risorsa e perché (il file, la riga e il motivo che
 * {@link GrammarBean} dà nella causa). Senza le grammatiche il gioco non può andare avanti: chi gestisce il ciclo di vita
 * (il motore) la prende e chiede alla piattaforma di terminare.
 *
 * @author Stefano Reksten
 */
public class GrammaticaNonValidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String risorsa;

	/**
	 * @param risorsa la risorsa della grammatica, per esempio {@code /com/threeamigos/foresta/testi/fiabe.txt}
	 * @param causa   l'errore di {@link GrammarBean} (grammatica non valida) o di lettura
	 */
	public GrammaticaNonValidaException(String risorsa, Throwable causa) {
		super("Grammatica dei testi non valida o mancante: " + risorsa + ": " + causa.getMessage(), causa);
		this.risorsa = risorsa;
	}

	/**
	 * La risorsa della grammatica che non si è potuta caricare.
	 */
	public String getRisorsa() {
		return risorsa;
	}
}
