package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il gargoyle: costrutto volante. È immune a tutti gli incantesimi e agli stati sanguinamento, infezione, avvelenamento e
 * immobilizzazione.
 */
public class Gargoyle extends PersonaggioBase {

	public Gargoyle(int livello) {
		super(TipoPersonaggio.GARGOYLE, livello);
	}

}
