package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * L'idra: bestia gigantesca, boss del suo castello. Parte con salute e magia piene; è immune agli incantesimi di Terra, Acqua e
 * Aria e agli stati confuso e stordito. Combatte soprattutto con le sue teste, cioè con le armi: di magie ne sa due, il Veleno e l'Acqua.
 */
public class Idra extends PersonaggioBase implements Personaggio {

	public TipoPersonaggio getClasse() { return TipoPersonaggio.IDRA; }

	public Idra(int livello) {
		super(TipoPersonaggio.IDRA, livello);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.VELENO, TipoIncantesimo.ACQUA);
	}

}
