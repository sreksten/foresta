package com.threeamigos.foresta.incantesimi;

public enum PortataIncantesimo {
	

	/**
	 * Un effetto su tutta la locazione o su chi lo formula, senza bersagli (al momento non ne esistono: un
	 * NegaMagia, un AccresciForza, "fino alla fine del turno gli attacchi sono ridotti del X%"...). Va scritto
	 * ridefinendo IncantesimoMaleficoImpl.formulaGlobale. Morte non può mai esserlo.
	 */
	GLOBALE,

	/**
	 * Colpisce tutti i personaggi vivi del gruppo avversario
	 */
	GRUPPO,

	/**
	 * Colpisce fino a N personaggi vivi del gruppo avversario, con N il numero di bersagli di chi lo formula
	 * (Personaggio.getBersagli)
	 */
	MULTIPLO,

	/**
	 * Su un solo personaggio: se l'incantesimo è benefico (Resurrezione) uno del nostro gruppo, scelto dal
	 * giocatore; se è malefico (Morte) un avversario, per ora il primo ancora vivo (vedi LocazioneBase).
	 * SOLO_VIVI esclude i morti, QUALSIASI no.
	 */
	SINGOLO_SOLO_VIVI,
	SINGOLO_QUALSIASI

}
