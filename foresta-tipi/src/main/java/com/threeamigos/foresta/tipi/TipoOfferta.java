package com.threeamigos.foresta.tipi;

/**
 * I servizi che un incontro amichevole o una locanda possono proporre. L'offerta vera la costruisce
 * offerte.FabbricaOfferte.
 */
public enum TipoOfferta {
	/** Un avversario si unisce al gruppo per un periodo limitato, senza chiedere niente */
	AIUTO_GRATUITO,
	/** Un avversario si unisce al gruppo per un periodo limitato, in cambio di monete */
	AIUTO_MERCENARIO,
	/** Pergamene di un incantesimo, da comprare a metà del prezzo abituale */
	INCANTESIMI,
	/** Notizie gratuite: dove si trova un artefatto smarrito, una città o un castello */
	INFORMAZIONI,
	/** La mappa completa della Foresta, a pagamento */
	MAPPA_FORESTA,
	/** La mappa della zona circostante, gratis */
	MAPPA_ZONA,
	/** Un pasto caldo e gratuito, che a volte non incontra i gusti del gruppo */
	PASTO;
}
