package com.threeamigos.foresta.tipi.tattiche;

/**
 * Com'è andata un'azione contro un bersaglio, per la memoria dello scontro (StatoDelCombattimento.registraEsitoAzione): le tattiche ci rinunciano
 * dopo troppi fallimenti, e non riprovano contro un'immunità scoperta (tattiche_di_combattimento_automatiche.md §11.7).
 */
public enum EsitoAzione {
	/** L'azione ha avuto effetto */
	RIUSCITA,
	/** Il tiro è fallito per caso: si può riprovare, ma non all'infinito */
	MANCATA,
	/** Il bersaglio è immune o resiste: lo si scopre una volta e vale per tutti */
	IMMUNE
}
