package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'eremita: umanoide, corrompibile e amichevole. Con l'amicizia offre aiuto, incantesimi, informazioni, mappe e un pasto;
 * con la corruzione, soltanto informazioni.
 */
public class Eremita extends PersonaggioBase {

	public Eremita(int livello) {
		super(TipoPersonaggio.EREMITA, livello);
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
		return new TipoOfferta[] {
				TipoOfferta.INFORMAZIONI,
		};
	}
}
