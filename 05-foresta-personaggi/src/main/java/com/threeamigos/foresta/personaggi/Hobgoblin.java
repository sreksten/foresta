package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'hobgoblin: creatura umanoide e corrompibile, ma non amichevole. Con la corruzione offre un aiuto a pagamento, incantesimi
 * o la mappa della Foresta.
 */
public class Hobgoblin extends PersonaggioBase {

	public Hobgoblin(int livello) {
		super(TipoPersonaggio.HOBGOBLIN, livello);
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.MAPPA_FORESTA,
		};
	}
}
