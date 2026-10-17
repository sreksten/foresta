package com.threeamigos.foresta.tipi;

/**
 * La categoria di un trofeo, per mostrarli raggruppati: l'ordine qui è quello in cui
 * compaiono nella pagina dei trofei.
 */
public enum CategoriaTrofeo {

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
