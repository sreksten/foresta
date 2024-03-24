package com.threeamigos.foresta.incantesimi;

public enum PortataIncantesimo {
	

	/**
	 * Se l'incantesimo e' globale non ha bisogno di parametri aggiuntivi
	 * (al momento non ne esistono ma non si sa mai, un NegaMagia o AccresciForza
	 * o qualcosa del genere che agisce su tutta la locazione o su chi lo formula)
	 */
	GLOBALE,

	/**
	 * Se l'incantesimo e' di gruppo ha bisogno di un gruppo su cui formularlo
	 */
	GRUPPO,

	/**
	 * Se l'incantesimo e' singolo ha bisogno di un personaggio su cui formularlo
	 * ed il personaggio appartiene al nostro gruppo
	 */
	SINGOLO_SOLO_VIVI,
	SINGOLO_QUALSIASI

};
