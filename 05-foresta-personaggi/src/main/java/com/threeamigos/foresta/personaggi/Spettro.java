package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Lo spettro: non morto volante, con le capacità comuni descritte in {@link NonMortoVolante}.
 */
public class Spettro extends NonMortoVolante {

	public Spettro(int livello) {
		super(TipoPersonaggio.SPETTRO, livello);
	}

}
