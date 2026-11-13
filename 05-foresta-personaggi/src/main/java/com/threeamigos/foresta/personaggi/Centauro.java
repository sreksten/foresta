package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il centauro: creatura umanoide e ibrida, corrompibile e amichevole. Con l'amicizia o con la corruzione può fare qualsiasi
 * genere di offerta.
 */
public class Centauro extends PersonaggioBase {

	public Centauro(int livello) {
		super(TipoPersonaggio.CENTAURO, livello);
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		// Tutte le offerte
		return TipoOfferta.values();
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		// Tutte le offerte
		return TipoOfferta.values();
	}
}
