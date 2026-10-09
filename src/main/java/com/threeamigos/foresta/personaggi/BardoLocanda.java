package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il bardo della locanda: ha le caratteristiche del bardo, ma un'altra immagine e un'altra icona. Come il Viandante
 * non si incontra nelle locazioni e non si recluta.
 */
public class BardoLocanda extends Bardo {

	public BardoLocanda(int livello) {
		this(null, livello);
	}

	public BardoLocanda(String nome, int livello) {
		super(nome, TipoPersonaggio.BARDO_LOCANDA, livello);
	}

	@Override
	public String getNomeSingolare() { return "Bardo della locanda"; }

	@Override
	public String getNomePlurale() { return "Bardi della locanda"; }
}
