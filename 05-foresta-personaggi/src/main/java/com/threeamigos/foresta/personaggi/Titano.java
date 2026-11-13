package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il titano: creatura umanoide e gigantesca, corrompibile e amichevole. Con l'amicizia offre aiuto, incantesimi, informazioni,
 * mappe e un pasto; con la corruzione può fare qualsiasi offerta.
 */
public class Titano extends PersonaggioBase {

	public Titano(int livello) {
		super(TipoPersonaggio.TITANO, livello);
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_GRATUITO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.MAPPA_FORESTA,
				TipoOfferta.MAPPA_ZONA,
				TipoOfferta.PASTO
		};
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		// Tutte le offerte
		return TipoOfferta.values();
	}
}
