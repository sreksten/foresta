package com.threeamigos.foresta.testi;

/**
 * Le preposizioni che si fondono con l'articolo ("di" + "il" = "del", "in" + "il" = "nel"). In una lingua che non le fonde
 * (l'inglese) restano staccate: "of the".
 */
public enum Preposizione {
	A, DI, DA, IN,
	/**
	 * Dove si arriva ("arriva a Fleena", "arrives in Fleena"): in italiano è «a», in inglese «in».
	 */
	ARRIVO,
	/**
	 * Vicino a un luogo ("vicino alle Rovine", "near the Ruins"): in italiano resta «a» dopo «vicino», in inglese non vuole preposizione.
	 */
	VICINO
}
