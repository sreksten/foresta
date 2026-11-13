package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Il folletto: creatura umanoide e volante, corrompibile e amichevole. Nel suo repertorio ci sono Acqua, Gelo e Aria; con
 * l'amicizia offre incantesimi, informazioni e un pasto.
 */
public class Folletto extends PersonaggioBase implements Personaggio {

	public Folletto(int livello) {
		super(TipoPersonaggio.FOLLETTO, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.ACQUA, TipoIncantesimo.GELO, TipoIncantesimo.ARIA);
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[] {
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.PASTO
		};
	}
}
