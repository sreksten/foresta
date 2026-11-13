package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * La chimera: bestia ibrida. Nel suo repertorio ci sono il Fuoco e il Veleno.
 */
public class Chimera extends PersonaggioBase {

	public Chimera(int livello) {
		super(TipoPersonaggio.CHIMERA, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.FUOCO, TipoIncantesimo.VELENO);
	}

}
