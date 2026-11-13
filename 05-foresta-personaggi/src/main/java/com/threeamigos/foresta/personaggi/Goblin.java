package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il goblin: creatura umanoide e corrompibile, ma non amichevole. Con la corruzione offre un aiuto a pagamento o la mappa
 * della Foresta.
 */
public class Goblin extends PersonaggioBase {

	public Goblin(int livello) {
		super(TipoPersonaggio.GOBLIN, livello);
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.MAPPA_FORESTA,
		};
	}
}
