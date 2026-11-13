package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * La viverna: bestia draconica e volante. Nel suo repertorio c'è il Veleno; è immune agli incantesimi di Fuoco.
 */
public class Viverna extends PersonaggioBase implements Personaggio {

	public Viverna(int livello) {
		super(TipoPersonaggio.VIVERNA, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.VELENO);
	}

}
