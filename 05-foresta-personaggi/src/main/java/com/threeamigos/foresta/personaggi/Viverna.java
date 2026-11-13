package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * La viverna: bestia draconica e volante. Nel suo repertorio c'è il Veleno; il fuoco non la ferisce, incantesimi compresi (vedi TipoPersonaggio).
 */
public class Viverna extends PersonaggioBase {

	public Viverna(int livello) {
		super(TipoPersonaggio.VIVERNA, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.VELENO);
	}

}
