package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il capitano delle guardie: ha le caratteristiche del guerriero, ma immagini e icona proprie. Non è una classe
 * giocante e non si recluta.
 */
public class CapitanoDelleGuardie extends Guerriero {

	public CapitanoDelleGuardie(int livello) {
		this(null, livello);
	}

	public CapitanoDelleGuardie(String nome, int livello) {
		super(nome, TipoPersonaggio.CAPITANO_DELLE_GUARDIE, livello);
	}

	@Override
	public String getNomeSingolare() { return "Capitano delle guardie"; }

	@Override
	public String getNomePlurale() { return "Capitani delle guardie"; }
}
