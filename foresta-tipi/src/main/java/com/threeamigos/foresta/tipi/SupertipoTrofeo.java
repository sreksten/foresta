package com.threeamigos.foresta.tipi;

/**
 * La tipologia di un trofeo, per mostrarli raggruppati: l'ordine qui è quello in cui
 * compaiono nella pagina dei trofei.
 */
public enum SupertipoTrofeo {

	/**
	 * Trofei che dipendono dagli altri trofei
	 */
	SPECIALE,
	/**
	 * Missioni portate a termine
	 */
	MISSIONE,
	/**
	 * Avversari sconfitti
	 */
	UCCISIONE,
	/**
	 * Tesori e artefatti tolti agli avversari, cofani aperti
	 */
	BOTTINO,
	/**
	 * Acquisti e servizi dei negozi di città
	 */
	ACQUISTO,
	/**
	 * Amicizie e corruzioni
	 */
	DIPLOMAZIA,
	/**
	 * Tutto il resto
	 */
	VARIE
}
