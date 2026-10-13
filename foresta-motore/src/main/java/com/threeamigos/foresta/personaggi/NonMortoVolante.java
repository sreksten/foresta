package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * I non morti volanti incorporei (fantasma, ombra nera, spettro, spirito): nel repertorio hanno il Gelo e, oltre a ciò che
 * vale per tutti i non morti, sono immuni agli incantesimi di Aria e di Terra e allo stato atterrato. Le classi concrete
 * aggiungono solo i loro valori.
 */
public abstract class NonMortoVolante extends PersonaggioBase implements Personaggio {

	NonMortoVolante(TipoPersonaggio classe, int livello) {
		super(classe, livello);
	}

	@Override
	public Set<ClasseIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(ClasseIncantesimo.GELO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return super.isImmuneAIncantesimo(classeIncantesimo) ||
				classeIncantesimo == ClasseIncantesimo.ARIA ||
				classeIncantesimo == ClasseIncantesimo.TERRA;
	}

	@Override
	public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
		return super.isImmuneAEffetto(effettoStato) ||
				effettoStato == TipoEffettoDiStato.ATTERRATO;
	}
}
