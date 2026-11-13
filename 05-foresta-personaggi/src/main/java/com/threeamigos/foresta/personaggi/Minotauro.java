package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il minotauro: creatura umanoide e ibrida, corrompibile ma non amichevole. Con la corruzione offre un aiuto a pagamento o la
 * mappa della Foresta. Per la sua versione gigante vedi {@link MinotauroGigante}.
 */
public class Minotauro extends PersonaggioBase {

	public Minotauro(int livello) {
		super(TipoPersonaggio.MINOTAURO, livello);
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[]{
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.MAPPA_FORESTA,
		};
	}

}
