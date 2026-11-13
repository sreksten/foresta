package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * La chimera drago: bestia ibrida e draconica. Nel suo repertorio ci sono il Fuoco e il Veleno, ed è immune al Fuoco e
 * allo stato bruciato.
 */
public class ChimeraDrago extends PersonaggioBase implements Personaggio {

	public ChimeraDrago(int livello) {
		super(TipoPersonaggio.CHIMERA_DRAGO, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.FUOCO, TipoIncantesimo.VELENO);
	}

}
