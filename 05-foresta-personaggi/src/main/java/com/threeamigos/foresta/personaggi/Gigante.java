package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il gigante: creatura umanoide e gigantesca, corrompibile e amichevole. Con l'amicizia offre aiuto, incantesimi,
 * informazioni, mappe e un pasto.
 */
public class Gigante extends PersonaggioBase implements Personaggio {

	public Gigante(int livello) {
		super(TipoPersonaggio.GIGANTE, livello);
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
}
