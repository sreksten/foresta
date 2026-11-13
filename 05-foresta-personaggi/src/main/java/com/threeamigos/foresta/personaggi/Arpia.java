package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * L'arpia: creatura umanoide, ibrida e volante. Nel suo repertorio c'è l'incantesimo dell'Aria; l'aria non la ferisce, incantesimi compresi.
 */
public class Arpia extends PersonaggioBase {

	public Arpia(int livello) {
		super(TipoPersonaggio.ARPIA, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.ARIA);
	}

}
