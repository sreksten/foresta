package com.threeamigos.foresta.intermezzi;

/**
 * Cosa fa l'animazione di un elemento quando ha percorso tutte le sue tappe.
 */
public enum Ripetizione {

	/** Si ferma sull'ultima tappa. */
	UNA_VOLTA,

	/** Ricomincia dalla prima tappa (salto allo stato iniziale). */
	CICLICA,

	/** Ripercorre le tappe al contrario, e così via avanti e indietro. */
	AVANTI_E_INDIETRO
}
