package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Il minotauro gigante: creatura umanoide, ibrida e gigantesca, boss del suo castello. Parte con salute piena. Combatte solo con le
 * armi: non ha Magia e non sa nessun incantesimo.
 */
public class MinotauroGigante extends PersonaggioBase {

	public MinotauroGigante(int livello) {
		super(TipoPersonaggio.MINOTAURO_GIGANTE, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.noneOf(TipoIncantesimo.class);
	}

}
