package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il fantasma: non morto volante, con le capacità comuni descritte in {@link NonMortoVolante}.
 */
public class Fantasma extends NonMortoVolante {

	public Fantasma(int livello) {
		super(TipoPersonaggio.FANTASMA, livello);
	}

}
