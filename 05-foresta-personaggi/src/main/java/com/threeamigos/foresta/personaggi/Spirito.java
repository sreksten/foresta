package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Lo spirito: non morto volante, ma amichevole, con le capacità comuni descritte in {@link NonMortoVolante}.
 */
public class Spirito extends NonMortoVolante {

	public Spirito(int livello) {
		super(TipoPersonaggio.SPIRITO, livello);
	}

}
