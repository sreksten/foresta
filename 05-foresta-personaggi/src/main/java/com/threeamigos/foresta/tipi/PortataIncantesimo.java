package com.threeamigos.foresta.tipi;

/**
 * Su chi agisce un incantesimo: su tutta la locazione, su un gruppo, su più bersagli o su uno solo.
 *
 * @author Stefano Reksten
 */
public enum PortataIncantesimo {

	/**
	 * Un effetto su tutta la locazione, senza un bersaglio scelto: agisce su ogni avversario vivo (Nebbia, Nega magia,
	 * Terremoto, Terrore), su tutti tranne chi lo formula (Tempesta, che colpisce anche i compagni) o su tutto il gruppo di
	 * chi lo formula (Grido di battaglia, benefico). Un incantesimo malefico lo scrive ridefinendo
	 * IncantesimoMaleficoImpl.formulaGlobale. Morte non può mai esserlo.
	 */
	GLOBALE,
	/**
	 * Colpisce tutti i personaggi vivi del gruppo avversario. L'unico incantesimo con questa portata, Silenzio, ridefinisce
	 * la scelta e ne silenzia uno solo (Silenzio.scegliBersaglio).
	 */
	GRUPPO,
	/**
	 * Colpisce fino a N personaggi vivi del gruppo avversario, con N il numero di bersagli di chi lo formula
	 * (Personaggio.getBersagli).
	 */
	MULTIPLO,
	/**
	 * Su un solo personaggio del nostro gruppo, anche se è morto: è la portata della Resurrezione.
	 */
	SINGOLO_QUALSIASI,
	/**
	 * Su un solo personaggio: se l'incantesimo è benefico uno del nostro gruppo, scelto dal giocatore;
	 * se è malefico (Morte) un avversario, per ora il primo ancora vivo (vedi LocazioneBase).
	 */
	SINGOLO_SOLO_VIVI
}
